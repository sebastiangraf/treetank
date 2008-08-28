/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Patent Pending.
 * 
 * NO permission to use, copy, modify, and/or distribute this software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id:sys_treetank_authentication.c 4360 2008-08-24 11:17:12Z kramis $
 */

#include "sys_treetank.h"

/* --- Function prototypes. ------------------------------------------------- */

int sys_treetank_authentication(u_int64_t, u_int8_t, u_int16_t *, u_int8_t *);
int sys_treetank_callback(struct cryptop *op);

/* --- Global variables. ---------------------------------------------------- */

static u_int8_t tt_auth_key[32] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

/*
 * Perform authentication.
 */
int
sys_treetank_authentication(
  u_int64_t  session,
  u_int8_t   command,
  u_int16_t *lengthPtr,
  u_int8_t  *bufferPtr)
{

  /* --- Local variables. --------------------------------------------------- */
  
  int             syscall      = TT_SYSCALL_SUCCESS;
  int             i            = 0x0;
  struct mbuf    *packetPtr    = NULL;
  struct cryptop *operationPtr = NULL;
  u_int16_t       myLength     = *lengthPtr - TT_REFERENCE_LENGTH;
  u_int8_t       *myHmacPtr    = bufferPtr + TT_HMAC_OFFSET;
  u_int8_t       *myBufferPtr  = bufferPtr + TT_REFERENCE_LENGTH;
  u_int8_t        hmac[TT_HMAC_LENGTH];
  
  /* --- Initialise buffer. ------------------------------------------------- */
  
  packetPtr = m_gethdr(M_DONTWAIT, MT_DATA);
  if (packetPtr == NULL) {
    syscall = TT_SYSCALL_FAILURE;
    printf("ERROR(sys_treetank_authentication.c): Could not allocate mbuf.\n");
    goto finish;
  }
  
  packetPtr->m_pkthdr.len = 0x0;
  packetPtr->m_len        = 0x0;
    
  m_copyback(
    packetPtr,
    0x0,
    myLength,
    myBufferPtr);

  /* --- Initialise crypto operation. --------------------------------------- */
  
  operationPtr = crypto_getreq(0x1);
  if (operationPtr == NULL) {
    syscall = TT_SYSCALL_FAILURE;
    printf("ERROR(sys_treetank_authentication.c): Could not allocate crypto.\n");
    goto finish;
  }

  operationPtr->crp_sid               = session;
  operationPtr->crp_ilen              = myLength;
  operationPtr->crp_flags             = CRYPTO_F_IMBUF;
  operationPtr->crp_buf               = (caddr_t) packetPtr;
  operationPtr->crp_desc->crd_alg     = TT_AUTHENTICATION_ALGORITHM;
  operationPtr->crp_desc->crd_klen    = TT_KEY_LENGTH;
  operationPtr->crp_desc->crd_key     = (caddr_t) &tt_auth_key;
  operationPtr->crp_desc->crd_skip    = 0x0;
  operationPtr->crp_desc->crd_len     = myLength;
  operationPtr->crp_desc->crd_inject  = 0x0;
  operationPtr->crp_mac               = (caddr_t) hmac;
  operationPtr->crp_callback          = 
        (int (*) (struct cryptop *)) sys_treetank_callback;
   
  /* --- Synchronously dispatch crypto operation. --------------------------- */
  
  crypto_dispatch(operationPtr);
  
  while (!(operationPtr->crp_flags & CRYPTO_F_DONE)) {
    syscall = tsleep(operationPtr, PSOCK, "sys_treetank_authentication", 0x0);
  }
  
  if (syscall != TT_SYSCALL_SUCCESS) {
    printf("ERROR(sys_treetank_authentication.c): Failed during tsleep.\n");
    goto finish;
  }
  
  if (operationPtr->crp_etype != TT_SYSCALL_SUCCESS) {
    syscall = operationPtr->crp_etype;
    printf("ERROR(sys_treetank_authentication.c): Failed during crypto_dispatch.\n");
    goto finish;
  }
  
  /* --- Collect result from buffer. ---------------------------------------- */
  
  packetPtr = operationPtr->crp_buf;
  
  if (command == TT_WRITE_FRAGMENT) {
    bcopy(hmac, myHmacPtr, TT_HMAC_LENGTH);
  } else {
    for (i = TT_HMAC_LENGTH - 0x1; i > 0x0; i--) {
      if (hmac[i] != myHmacPtr[i]) {
        *lengthPtr = 0x0;
        syscall = TT_SYSCALL_FAILURE;
        printf("ERROR(sys_treetank_authentication.c): Failed during crypto_dispatch.\n");
        goto finish;
      }
    }
  }
  
  /* --- Cleanup for all conditions. ---------------------------------------- */
  
finish:

  if (packetPtr != NULL) 
    m_freem(packetPtr);
 
  if (operationPtr != NULL)
    crypto_freereq(operationPtr);
  
  return (syscall);
}
