gcc -c main.c -o main.o
gcc -c dllmain.c -o dllmain.o
gcc -shared main.o dllmain.o -o injection.dll
del main.o
del dllmain.o
pause