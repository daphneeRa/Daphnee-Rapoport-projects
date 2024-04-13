all: clean compile link

compile:
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Order.o src/Order.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/main.o src/main.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Action.o src/Action.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Customer.o src/Customer.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Volunteer.o src/Volunteer.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/WareHouse.o src/WareHouse.cpp

link:
	g++ -g -Wall -Weffc++ -std=c++11 -o bin/warehouse bin/*.o

clean:
	rm -rf bin/*.o bin/warehouse
	clear

