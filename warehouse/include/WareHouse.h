#pragma once
#include <string>
#include <vector>

#include "../include/Order.h"
#include "../include/Customer.h"

class BaseAction;
class Volunteer;

// Warehouse responsible for Volunteers, Customers Actions, and Orders.


class WareHouse {

    public:
        WareHouse(const string &configFilePath); //add a call to a file reader ant then to the parser
        ~WareHouse(); //destructor
        WareHouse(const WareHouse &other); // copy constructor
        WareHouse& operator=(const WareHouse&other); //Assignment operator + with option to use few ==
        WareHouse(WareHouse&& other); //move constructor
        const WareHouse& operator= (WareHouse&& other); //move assingment operator
        vector<vector<string>> parser(const string &path);
        void backupFunc();
        void restore();
        void start();
        void addOrder(Order* order);
        void addAction(BaseAction* action);
        void addCustomer(Customer* customer);
        Customer &getCustomer(int customerId) const;
        Volunteer &getVolunteer(int volunteerId) const;
        bool isVolunteerExist(int volunteerId) const;
        Order &getOrder(int orderId) const; 
        int getOrderCounter() const;
        int getCustomerCounter() const;
        int getVolunteerCounter() const;
        vector<Order*> getPendingOrders() const;
        vector<Order*> getInProcessOrders() const;
        vector<Order*> getCompletedOrders() const;
        vector<Volunteer*> getVolunteers() const;
        vector<Customer*> getCustomers() const;
        void deleteVolunteer(int volunteerId);
        const vector<BaseAction*> &getActions() const;
        void close();
        void open();

    private:
        bool isOpen;
        vector<BaseAction*> actionsLog;
        vector<Volunteer*> volunteers;
        vector<Order*> pendingOrders;
        vector<Order*> inProcessOrders;
        vector<Order*> completedOrders;
        vector<Customer*> customers;
        int customerCounter; //For assigning unique customer IDs
        int volunteerCounter; //For assigning unique volunteer IDs
        int orderCounter;
};