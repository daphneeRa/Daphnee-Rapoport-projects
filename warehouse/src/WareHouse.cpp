#include <string>
#include <vector>
#include <iostream>
#include <sstream>
#include <fstream>
#include "../include/WareHouse.h"
#include "../include/Order.h"
#include "../include/Customer.h"
#include "../include/Action.h"
#include "../include/Volunteer.h"
using namespace std;
extern WareHouse* backup;

//Constructor
WareHouse::WareHouse(const string &configFilePath): 
isOpen(false), actionsLog(), volunteers(), pendingOrders(), inProcessOrders(), completedOrders(), customers() ,customerCounter(0), volunteerCounter(0), orderCounter(0)
{
    vector<vector<string>> allFile = parser(configFilePath); //parser
    for(auto line : allFile){
        string &type = line[2];
        string &name = line[1];
        int param1 = (line.size() > 3) ? stoi(line[3]) : 0;
        int param2 = (line.size() > 4) ? stoi(line[4]) : 0;
        int param3 = (line.size() > 5) ? stoi(line[5]) : 0;

        if(type == "soldier"){
            SoldierCustomer* customer = new SoldierCustomer(customerCounter, name, param1, param2);
            customerCounter++;
            customers.push_back(customer);
        }
        else if(type == "civilian"){
            CivilianCustomer* customer = new CivilianCustomer(customerCounter, name, param1, param2);
            customerCounter++;
            customers.push_back(customer);
        }
        else if(type == "collector"){
            CollectorVolunteer* volunteer = new CollectorVolunteer(volunteerCounter, name, param1);
            volunteerCounter++;
            volunteers.push_back(volunteer);
        }
        else if(type == "limited_collector"){
            LimitedCollectorVolunteer* volunteer = new LimitedCollectorVolunteer(volunteerCounter, name, param1, param2);
            volunteerCounter++;
            volunteers.push_back(volunteer);
        }
        else if(type == "driver"){
            DriverVolunteer* volunteer = new DriverVolunteer(volunteerCounter, name, param1, param2);
            volunteerCounter++;
            volunteers.push_back(volunteer);
        }
        else if(type == "limited_driver"){
            LimitedDriverVolunteer* volunteer = new LimitedDriverVolunteer(volunteerCounter, name, param1, param2, param3);
            volunteerCounter++;
            volunteers.push_back(volunteer);
        }
    }
};

//Rule Of 5:
//WareHouse Destructor
    WareHouse::~WareHouse(){
        //deep delete - deletes all the complex objects
        for (BaseAction *action : actionsLog){
            delete action;
        }
        for (Volunteer *volunteer : volunteers){
            delete volunteer;
        }
        for (Order *order : pendingOrders){
            delete order;
        }
        for (Order *order : inProcessOrders){
            delete order;
        }
        for (Order *order : completedOrders){
            delete order;
        }
        for (Customer *customer : customers){
            delete customer;
        }
        //delete all the null pointers 
        actionsLog.clear();
        volunteers.clear();
        pendingOrders.clear();
        inProcessOrders.clear();
        completedOrders.clear();
        customers.clear();
        }
//Copy Constructor
    WareHouse::WareHouse(const WareHouse &other): 
    isOpen(other.isOpen), actionsLog(), volunteers(), pendingOrders(), inProcessOrders(), completedOrders(), customers(),customerCounter(other.customerCounter),volunteerCounter(other.volunteerCounter), orderCounter(other.orderCounter){
         for (BaseAction *action : other.actionsLog){
            actionsLog.push_back(action->clone());
        }
        for (Volunteer *volunteer:other.volunteers){
            volunteers.push_back(volunteer->clone());
        }
        for (Order *order:other.pendingOrders){
            pendingOrders.push_back(new Order(*order));
        }
        for (Order *order:other.inProcessOrders){
            inProcessOrders.push_back(new Order(*order));
        }
        for (Order *order:other.completedOrders){
            completedOrders.push_back(new Order(*order));
        }
        for (Customer *customer:other.customers){
            customers.push_back(customer->clone());
        }
    }
    //Copy Assignment Operator 
     WareHouse& WareHouse::operator=(const WareHouse& other){
        if (this!=&other){
            isOpen= other.isOpen;
            customerCounter=other.customerCounter;
            volunteerCounter =other.volunteerCounter;
            orderCounter=other.orderCounter;
            //delete and switch with other
            for (BaseAction *action : this->actionsLog){
                delete action;
            }
            this->actionsLog.clear();
            for (BaseAction *action:other.actionsLog){
                this->actionsLog.push_back(action->clone());
            }

            for (Volunteer *volunteer :this->volunteers){
                delete volunteer;
            }
            this->volunteers.clear();
            for (Volunteer *volunteer:other.volunteers){
                this->volunteers.push_back(volunteer->clone());
            }

            for (Order *order : this->pendingOrders){
                delete order;
            }
            pendingOrders.clear();
            for (Order *order:other.pendingOrders){
            this->pendingOrders.push_back(new Order(*order));
            }
    
            for (Order *order : this->inProcessOrders){
                delete order;
            }
            inProcessOrders.clear();
             for (Order *order:other.inProcessOrders){
            this->inProcessOrders.push_back(new Order(*order));
            }

            for (Order *order :this->completedOrders){
                delete order;
            } 
            completedOrders.clear();
            for (Order *order:other.completedOrders){
            this->completedOrders.push_back(new Order(*order));
            }

            for (Customer *customer :this->customers){
                delete customer;
            }
            customers.clear();
            for (Customer *customer:other.customers){
            this->customers.push_back(customer->clone());
            }
        }
    return *this;}
    //Move Constructor
     WareHouse::WareHouse (WareHouse&& other):isOpen(other.isOpen),actionsLog(other.actionsLog),volunteers(other.volunteers),pendingOrders(other.pendingOrders),inProcessOrders(other.inProcessOrders),completedOrders(other.completedOrders),customers(other.customers), customerCounter(other.customerCounter),volunteerCounter(other.volunteerCounter),orderCounter(other.orderCounter){
        other.actionsLog.clear();
        other.volunteers.clear();
        other.pendingOrders.clear();
        other.inProcessOrders.clear();
        other.completedOrders.clear();
        other.customers.clear();
     };
    //Move Assignment Operator 
    const WareHouse& WareHouse::operator= (WareHouse&& other){ 
       if (this!=&other){
            isOpen= other.isOpen;
            customerCounter=other.customerCounter;
            volunteerCounter =other.volunteerCounter;
            orderCounter=other.orderCounter;
            //delete and switch with other
            for (BaseAction *action : this->actionsLog){
                delete action;
            }
            actionsLog.clear();
            for (BaseAction *action:other.actionsLog){
                this->actionsLog.push_back(action->clone());
            }  

            for (Volunteer *volunteer :this->volunteers){
                delete volunteer;
            }
            volunteers.clear();
            for (Volunteer *volunteer:other.volunteers){
                this->volunteers.push_back(volunteer->clone());
            }

            for (Order *order : this->pendingOrders){
                delete order;
            }
            pendingOrders.clear();
            for (Order *order:other.pendingOrders){
            this->pendingOrders.push_back(new Order(*order));
            }
    
            for (Order *order : this->inProcessOrders){
                delete order;
            }
            inProcessOrders.clear();
             for (Order *order:other.inProcessOrders){
            this->inProcessOrders.push_back(new Order(*order));
            }

            for (Order *order :this->completedOrders){
                delete order;
            } 
            completedOrders.clear();
            for (Order *order:other.completedOrders){
            this->completedOrders.push_back(new Order(*order));
            }

            for (Customer *customer :this->customers){
                delete customer;
            }
            customers.clear();
            for (Customer *customer:other.customers){
            this->customers.push_back(customer->clone());
            }
            other.actionsLog.clear();
            other.volunteers.clear();
            other.pendingOrders.clear();
            other.inProcessOrders.clear();
            other.completedOrders.clear();
            other.customers.clear();
       }
        return *this;
    }

//Parser
vector<vector<string>> WareHouse::parser(const string &path){
    vector<vector<string>> allFile;
    ifstream file(path);
    if (!file.is_open()) {
        cerr << "Error opening file" << endl;
        return allFile;
    }
    string line; 
    while (getline(file, line)) {
    // Skip comments and empty lines
        if (!line.empty() && line[0] != '#') {
            istringstream iss(line);
            vector<string> words;
            while(iss >> skipws >> line){
                if(line[0] == '#'){ //will stop the lop if a # is found in the middle of a line
                    break;}
                words.push_back(line);
            }
            allFile.push_back(words);
        } 
    }
    file.close();
    return allFile;
};

void WareHouse::backupFunc() {
    if(backup == nullptr){
        backup = new WareHouse(*this);
    }
    else{
        *backup = *this;
    }
}

void WareHouse::restore(){
    *this = *backup;
}

void WareHouse::start(){
    
    open(); 
    std::cout << "Warehouse is open!\n";
    string userInput;
    while (userInput != "close") {
        getline(cin, userInput);
        vector<string> words;
        istringstream stream(userInput);
        string word;
        while (stream >> word) { //runs through the words in the string
            words.push_back(word); //adds each word to the vector. 
        }
        if(words[0] == "step"){
            SimulateStep* step = new SimulateStep(std::stoi(words[1]));
            step->act(*this);
        }
        else if(words[0] == "customer"){
            AddCustomer* customer = new AddCustomer(words[1], words[2], std::stoi(words[3]), std::stoi(words[4]));
            customer->act(*this);
        }
        else if(words[0] == "order"){
            AddOrder* addOrder = new AddOrder(std::stoi(words[1]));
            addOrder->act(*this);
        }
        else if(words[0] == "orderStatus"){
            PrintOrderStatus* orderStat = new PrintOrderStatus (std::stoi(words[1]));
            orderStat->act(*this);
        }
        else if(words[0] == "customerStatus"){
            PrintCustomerStatus* customerStat = new PrintCustomerStatus (std::stoi(words[1]));
            customerStat->act(*this);
        }
        else if(words[0] == "volunteerStatus"){
            PrintVolunteerStatus* volunteerStat= new PrintVolunteerStatus(std::stoi(words[1]));
            volunteerStat->act(*this);
        }
        else if(words[0] == "log"){
            PrintActionsLog* log = new PrintActionsLog();
            log->act(*this);
        } 
        else if(words[0] == "backup"){
            BackupWareHouse* backup = new BackupWareHouse();
            backup->act(*this);
        }
        else if(words[0] == "restore"){
            RestoreWareHouse* restore = new RestoreWareHouse();
            restore->act(*this);
        }
    }
    Close* close = new Close();
    close->act(*this);
};


void WareHouse::addOrder(Order* order) {
    //add a new order
    if(order->getStatus() == OrderStatus::PENDING){
        pendingOrders.push_back(order);
        orderCounter++;
        getCustomer(order->getCustomerId()).addOrder(order->getId());
    }
    else if(order->getStatus() == OrderStatus::COLLECTING){
        //will happend when a collector recieved the order
        if(getVolunteer(order->getCollectorId()).isBusy()){
            int counter = -1;
            for(Order* order2 : pendingOrders){
                counter++;
                if(order2->getId() == order->getId()){
                    inProcessOrders.push_back(order);
                    pendingOrders.erase(pendingOrders.begin() + counter);
                }
            }
        }
        //will happend when a collector finished with an order
        else{
            int counter = -1;
            for(Order* order2 : inProcessOrders){
                counter++;
                if(order2->getId() == order->getId()){
                    pendingOrders.push_back(order);
                    inProcessOrders.erase(inProcessOrders.begin() + counter);
                }
            }
        }
    }
    //will happend when a driver recieved the order
    else if(order->getStatus() == OrderStatus::DELIVERING){
        int counter = -1;
        for(Order* order2 : pendingOrders){
            counter++;
            if(order2->getId() == order->getId()){
                inProcessOrders.push_back(order);
                pendingOrders.erase(pendingOrders.begin() + counter);
            }
        }
    }
    //will happend when a driver finished with an order
    else if(order->getStatus() == OrderStatus::COMPLETED){
        int counter = -1;
        for(Order* order2 : inProcessOrders){
            counter++;
            if(order2->getId() == order->getId()){
                completedOrders.push_back(order);
                inProcessOrders.erase(inProcessOrders.begin() + counter);
            }
        }  
    }
};

void WareHouse::addAction(BaseAction* action){
    actionsLog.push_back(action);
};

void WareHouse::addCustomer(Customer* customer) {
    customers.push_back(customer);
    customerCounter++;
};

Customer& WareHouse::getCustomer(int customerId) const {
    return *customers.at(customerId);
};

Volunteer& WareHouse::getVolunteer(int volunteerId) const {
    for(auto volunteer : volunteers){
        if(volunteer->getId() == volunteerId){
            return *volunteer;
        }
    }
    throw out_of_range("Volunteer doesn't exist"); //not supposed to happend 
};

bool WareHouse::isVolunteerExist(int volunteerId) const {
    for(auto volunteer : volunteers){
        if(volunteer->getId() == volunteerId){
            return true;
        }
    }
    return false;
};

Order& WareHouse::getOrder(int orderId) const {
    for(Order* order : pendingOrders){
        if(order->getId() == orderId){
            return *order;
        }
    }
    for(Order* order : inProcessOrders){
        if(order->getId() == orderId){
            return *order;
        }
    }
    for(Order* order : completedOrders){
        if(order->getId() == orderId){
            return *order;
        }
    }
    throw out_of_range("Order doesn't exist"); //not supposed to happend 
}; 

int WareHouse::getOrderCounter() const{
    return orderCounter;
};
int WareHouse::getCustomerCounter() const {
    return customerCounter;
};
int WareHouse::getVolunteerCounter() const {
    return volunteerCounter;
}; 
vector<Order*> WareHouse::getPendingOrders() const {
    return pendingOrders;
};
vector<Order*> WareHouse::getInProcessOrders() const {
    return inProcessOrders;
};
vector<Order*> WareHouse::getCompletedOrders() const {
    return completedOrders;
};
vector<Volunteer*> WareHouse::getVolunteers() const {
    return volunteers;
};
vector<Customer*> WareHouse::getCustomers() const {
    return customers;
};
void WareHouse::deleteVolunteer(int volunteerId) {
    int counter = -1;
    for(auto volunteer : volunteers){
        counter ++;
        if(volunteer->getId() == volunteerId){
            volunteers.erase(volunteers.begin() + counter);
        }
    }
};
const vector<BaseAction*> &WareHouse::getActions() const {
    return actionsLog;
};
void WareHouse::close() {
    isOpen = false;
};
void WareHouse::open() {
    isOpen = true;
};