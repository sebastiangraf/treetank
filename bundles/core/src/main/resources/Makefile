# OpenBSD makefile fir libTreeTank.so

target/libTreeTank.so : src/main/openbsd/NativeTreeTank.c src/main/openbsd/NativeTreeTank.h
	gcc -shared -Wall -I/treetank/jre/include/ -o target/libTreeTank.so src/main/openbsd/NativeTreeTank.c
