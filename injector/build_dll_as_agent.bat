gcc -c main.c -o main.o
g++ -c agent.cpp -o agent.o
g++ -shared main.o agent.o -o agent.dll
pause