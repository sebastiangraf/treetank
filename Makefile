# OpenBSD makefile fir libTreeTank.so

build : target/openbsd/libTreeTank.so

target/openbsd/libTreeTank.so : src/main/openbsd/NativeTreeTank.c src/main/openbsd/NativeTreeTank.h
	gcc -shared -Wall -I/treetank/jre/include/ -o target/openbsd/libTreeTank.so src/main/openbsd/NativeTreeTank.c
