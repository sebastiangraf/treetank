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
 * $Id$
 */

#include <unistd.h>
#include <stdio.h>
#include <fcntl.h>
#include <err.h>
#include <errno.h>
#include <string.h>

#define LENGTH 8569

int
main(void)
{

  int i,fd;
  u_int16_t length;
  u_int8_t buffer[1024];
  
  bzero(&buffer, sizeof(buffer));
  
  buffer[0] = 1;
  buffer[1] = 2;
  buffer[2] = 1;
  buffer[3] = 2;
  buffer[4] = 1;
  buffer[5] = 2;
  buffer[6] = 1;
  buffer[7] = 2;
  
  length = LENGTH;
       
  //fd = open("output", O_RDWR | O_CREAT, 0);
  //write(fd, &buffer, length);
  //close(fd);
  
  printf("Length before compression : %d\n", length);
  printf("Buffer before compression : '");
  for (i = 0; i < 32; i++)
  {
    printf("%x,", buffer[i]);
  }
  printf("'\n");
  
  length = syscall(306, 0x1, (0x1 | 0x10), length, &buffer);  /* compress */
  
  printf("Length after compression  : %d\n", length);
  printf("Buffer after compression  : '");
  for (i = 0; i < 32; i++)
  {
    printf("%x,", buffer[i]);
  }
  printf("'\n");

  length = syscall(306, 0x1, (0x2 | 0x20), length, &buffer); /* decompress */
 
  printf("Length after decompression: %d\n", length);
  printf("Buffer after decompression: '");
  for (i = 0; i < 32; i++)
  {
    printf("%x,", buffer[i]);
  }
  printf("'\n");
    
  return (0);
}
