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

#define TT_OK 99
#define TT_READ 111
#define TT_WRITE 100
#define TT_READ_INT(X) 0;
#define TT_WRITE_INT(X,Y) ;

/* --- Function prototypes. ------------------------------------------------- */

int sys_treetank_encryption(u_int8_t, u_int8_t, u_int8_t *, u_int8_t *);
int sys_treetank_encryption_callback(struct cryptop *op);

/* --- Global variables. ---------------------------------------------------- */

static u_int64_t tt_enc_sessionId[] = {
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION };
  
static u_int8_t tt_enc_key[32] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
static u_int8_t tt_enc_iv[16] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

/*
 * Perform encryption.
 */
int
sys_treetank_encryption(
  u_int8_t core,
  u_int8_t operation,
  u_int8_t *lengthPointer,
  u_int8_t *bufferPointer)
{

  /* --- Local variables. --------------------------------------------------- */
  
  u_int8_t        padding          = 0;
  int             error            = TT_OK;
  struct mbuf    *packetPointer    = NULL;
  struct cryptop *operationPointer = NULL;
  u_int32_t       length           = TT_READ_INT(lengthPointer);
  
  /* --- Initialise session (if required). ---------------------------------- */
    
  if (tt_enc_sessionId[core] == TT_NULL_SESSION)
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
          0) != TT_OK) {
      error = TT_ERROR;
      tt_enc_sessionId[core] = TT_NULL_SESSION;
      printf("ERROR(sys_treetank_encryption.c): Could not allocate cryptoini.\n");
      goto finish;
    }
    
  }
  
  /* --- Assure padding as described by Schneier (only for encryption) ------ */
  
  if (operation == TT_WRITE) {
    padding = TT_BLOCK_LENGTH - (length % TT_BLOCK_LENGTH);
    if (padding == 0)
      padding = TT_BLOCK_LENGTH;
    bzero(bufferPointer + length, padding);
    length += padding;
    bufferPointer[length - 1] = padding;
    TT_WRITE_INT(lengthPointer, length);
  }
  
  /* --- Initialise buffer. ------------------------------------------------- */
  
  packetPointer = m_gethdr(M_DONTWAIT, MT_DATA);
  if (packetPointer == NULL) {
    error = TT_ERROR;
    printf("ERROR(sys_treetank_encryption.c): Could not allocate mbuf.\n");
    goto finish;
  }
  
  packetPointer->m_pkthdr.len = 0;
  packetPointer->m_len        = 0;
    
  m_copyback(
    packetPointer,
    0,
    length,
    bufferPointer);

  /* --- Initialise crypto operation. --------------------------------------- */
  
  operationPointer = crypto_getreq(1);
  if (operationPointer == NULL) {
    error = TT_ERROR;
    printf("ERROR(sys_treetank_encryption.c): Could not allocate crypto.\n");
    goto finish;
  }

  operationPointer->crp_sid               = tt_enc_sessionId[core];
  operationPointer->crp_ilen              = length;
  operationPointer->crp_flags             = CRYPTO_F_IMBUF;
  operationPointer->crp_buf               = (caddr_t) packetPointer;
  operationPointer->crp_desc->crd_alg     = TT_ENCRYPTION_ALGORITHM;
  operationPointer->crp_desc->crd_klen    = TT_KEY_LENGTH;
  operationPointer->crp_desc->crd_key     = (caddr_t) &tt_enc_key;
  operationPointer->crp_desc->crd_skip    = 0;
  operationPointer->crp_desc->crd_len     = length;
  operationPointer->crp_desc->crd_inject  = 0;
  bcopy(tt_enc_iv, operationPointer->crp_desc->crd_iv, TT_BLOCK_LENGTH);
  if (operation == TT_WRITE)
    operationPointer->crp_desc->crd_flags = CRD_F_ENCRYPT | CRD_F_IV_PRESENT | CRD_F_IV_EXPLICIT;
  else
    operationPointer->crp_desc->crd_flags = 0 | CRD_F_IV_EXPLICIT;
  operationPointer->crp_callback          = (int (*) (struct cryptop *)) sys_treetank_encryption_callback;
   
  /* --- Synchronously dispatch crypto operation. --------------------------- */
  
  crypto_dispatch(operationPointer);
  
  while (!(operationPointer->crp_flags & CRYPTO_F_DONE)) {
    error = tsleep(operationPointer, PSOCK, "sys_treetank_encryption", 0);
  }
  
  if (error != TT_OK) {
    printf("ERROR(sys_treetank_encryption.c): Failed during tsleep.\n");
    goto finish;
  }
  
  if (operationPointer->crp_etype != TT_OK) {
    error = operationPointer->crp_etype;
    printf("ERROR(sys_treetank_encryption.c): Failed during crypto_dispatch.\n");
    goto finish;
  }
  
  /* --- Collect result from buffer. ---------------------------------------- */
  
  packetPointer = operationPointer->crp_buf;
  
  m_copydata(
    packetPointer,
    0,
    length,
    bufferPointer);
    
  /* --- Assure padding as described by Schneier (only for decryption) ------ */
  
  if (operation == TT_READ) {
    padding = bufferPointer[length - 1];
    if (padding < 1 || padding > TT_BLOCK_LENGTH) {
      error = TT_ERROR;
      printf("ERROR(sys_treetank_encryption.c): Failed during crypto_dispatch.\n");
      goto finish;
    }
    length -= padding;
    TT_WRITE_INT(lengthPointer, length);
  }
  
  /* --- Cleanup for all conditions. ---------------------------------------- */
  
finish:

  if (packetPointer != NULL)
    m_freem(packetPointer);
 
  if (operationPointer != NULL)
    crypto_freereq(operationPointer);
  
  return (error);
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
  return (TT_OK);
  
}
