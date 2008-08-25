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
 * $Id:sys_treetank_encryption.c 4360 2008-08-24 11:17:12Z kramis $
 */

#include "sys_treetank.h"

/* --- Function prototypes. ------------------------------------------------- */

int sys_treetank_encryption(u_int8_t, u_int8_t, u_int16_t *, u_int8_t *);
int sys_treetank_encryption_callback(struct cryptop *op);

/* --- Global variables. ---------------------------------------------------- */

static u_int64_t tt_enc_sessionId[] = {
  TT_NULL, TT_NULL, TT_NULL, TT_NULL,
  TT_NULL, TT_NULL, TT_NULL, TT_NULL,
  TT_NULL, TT_NULL, TT_NULL, TT_NULL,
  TT_NULL, TT_NULL, TT_NULL };
  
static u_int8_t tt_enc_key[32] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
static u_int8_t tt_enc_iv[16] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

/*
 * Perform encryption.
 */
int
sys_treetank_encryption(
  u_int8_t   core,
  u_int8_t   command,
  u_int16_t *lengthPtr,
  u_int8_t  *bufferPtr)
{

  /* --- Local variables. --------------------------------------------------- */
  
  int             syscall      = TT_SYSCALL_SUCCESS;
  struct mbuf    *packetPtr    = NULL;
  struct cryptop *operationPtr = NULL;
  u_int8_t        padding      = 0x0;
  u_int16_t       myLength     = *lengthPtr - TT_REFERENCE_LENGTH;
  u_int8_t       *myBufferPtr  = bufferPtr + TT_REFERENCE_LENGTH;
  
  /* --- Initialise session (if required). ---------------------------------- */
    
  if (tt_enc_sessionId[core] == TT_NULL)
  {
    struct cryptoini session;  

    bzero(&session, sizeof(session));
    session.cri_alg  = TT_ENCRYPTION_ALGORITHM;
    session.cri_klen = TT_KEY_LENGTH;
    session.cri_rnd  = TT_ROUNDS;
    session.cri_key  = (caddr_t) &tt_enc_key;
    
    if (crypto_newsession(
          &(tt_enc_sessionId[core]),
          &session,
          0x0) != TT_SYSCALL_SUCCESS) {
      syscall = TT_SYSCALL_FAILURE;
      tt_enc_sessionId[core] = TT_NULL;
      printf("ERROR(sys_treetank_encryption.c): Could not allocate cryptoini.\n");
      goto finish;
    }
    
  }
  
  /* --- Assure padding as described by Schneier (only for encryption) ------ */
  
  if (command == TT_COMMAND_WRITE) {
    padding = TT_BLOCK_LENGTH - (myLength % TT_BLOCK_LENGTH);
    if (padding == 0x0)
      padding = TT_BLOCK_LENGTH;
    bzero(myBufferPtr + myLength, padding);
    myLength += padding;
    myBufferPtr[myLength - 0x1] = padding;
    *lengthPtr = myLength + TT_REFERENCE_LENGTH;
  }
  
  /* --- Initialise buffer. ------------------------------------------------- */
  
  packetPtr = m_gethdr(M_DONTWAIT, MT_DATA);
  if (packetPtr == NULL) {
    syscall = TT_SYSCALL_FAILURE;
    printf("ERROR(sys_treetank_encryption.c): Could not allocate mbuf.\n");
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
    printf("ERROR(sys_treetank_encryption.c): Could not allocate crypto.\n");
    goto finish;
  }

  operationPtr->crp_sid               = tt_enc_sessionId[core];
  operationPtr->crp_ilen              = myLength;
  operationPtr->crp_flags             = CRYPTO_F_IMBUF;
  operationPtr->crp_buf               = (caddr_t) packetPtr;
  operationPtr->crp_desc->crd_alg     = TT_ENCRYPTION_ALGORITHM;
  operationPtr->crp_desc->crd_klen    = TT_KEY_LENGTH;
  operationPtr->crp_desc->crd_key     = (caddr_t) &tt_enc_key;
  operationPtr->crp_desc->crd_skip    = 0x0;
  operationPtr->crp_desc->crd_len     = myLength;
  operationPtr->crp_desc->crd_inject  = 0x0;
  bcopy(tt_enc_iv, operationPtr->crp_desc->crd_iv, TT_BLOCK_LENGTH);
  if (command == TT_COMMAND_WRITE)
    operationPtr->crp_desc->crd_flags =
          CRD_F_ENCRYPT | CRD_F_IV_PRESENT | CRD_F_IV_EXPLICIT;
  else
    operationPtr->crp_desc->crd_flags =
          0x0 | CRD_F_IV_EXPLICIT;
  operationPtr->crp_callback          = 
        (int (*) (struct cryptop *)) sys_treetank_encryption_callback;
   
  /* --- Synchronously dispatch crypto operation. --------------------------- */
  
  crypto_dispatch(operationPtr);
  
  while (!(operationPtr->crp_flags & CRYPTO_F_DONE)) {
    syscall = tsleep(operationPtr, PSOCK, "sys_treetank_encryption", 0x0);
  }
  
  if (syscall != TT_SYSCALL_SUCCESS) {
    printf("ERROR(sys_treetank_encryption.c): Failed during tsleep.\n");
    goto finish;
  }
  
  if (operationPtr->crp_etype != TT_SYSCALL_SUCCESS) {
    syscall = operationPtr->crp_etype;
    printf("ERROR(sys_treetank_encryption.c): Failed during crypto_dispatch.\n");
    goto finish;
  }
  
  /* --- Collect result from buffer. ---------------------------------------- */
  
  packetPtr = operationPtr->crp_buf;
  
  m_copydata(
    packetPtr,
    0,
    myLength,
    myBufferPtr);
    
  /* --- Assure padding as described by Schneier (only for decryption) ------ */
  
  if (command == TT_COMMAND_READ) {
    padding = myBufferPtr[myLength - 0x1];
    if (padding < 0x1 || padding > TT_BLOCK_LENGTH) {
      syscall = TT_SYSCALL_FAILURE;
      printf("ERROR(sys_treetank_encryption.c): Failed during crypto_dispatch.\n");
      goto finish;
    }
    myLength -= padding;
    *lengthPtr = myLength + TT_REFERENCE_LENGTH;
  }
  
  /* --- Cleanup for all conditions. ---------------------------------------- */
  
finish:

  if (packetPtr != NULL)
    m_freem(packetPtr);
 
  if (operationPtr != NULL)
    crypto_freereq(operationPtr);
  
  return (syscall);
}

/*
 * Callback to retrieve result of crypto operation.
 */
int
sys_treetank_encryption_callback(struct cryptop *op)
{
  
  if (op->crp_etype == EAGAIN) {
    op->crp_flags = CRYPTO_F_IMBUF;
    return crypto_dispatch(op);
  }
  
  wakeup(op);
  return (TT_SYSCALL_SUCCESS);
  
}
