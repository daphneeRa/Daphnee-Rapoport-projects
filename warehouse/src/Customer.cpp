#include "Customer.h"
#include <string>
#include <vector>
#include "Volunteer.h"
using std::string;
using std::vector;

//Customer Constructor
       Customer::Customer(int _id, const string &_name, int _locationDistance, int _maxOrders):id(_id), name(_name), locationDistance(_locationDistance), maxOrders(_maxOrders), ordersId(){}
//Customer COPY Constructor
       Customer::Customer(const Customer& other):id(other.id), name(other.name), locationDistance(other.locationDistance), maxOrders(other.maxOrders), ordersId(other.ordersId){};
//Customer destractor
       Customer::~Customer(){}; 
//Customer Methods
        const string& Customer::getName()const{return name;}
        int Customer::getId() const {return id;}
        int Customer::getCustomerDistance() const {return locationDistance;}
        int Customer::getMaxOrders() const{return maxOrders;} //Returns maxOrders
        int Customer::getNumOrders() const{return ordersId.size();} //Returns num of orders the customer has made so far
        bool Customer::canMakeOrder()const {return (ordersId.size()!= static_cast<std::vector<int>::size_type>(maxOrders));} //Returns true if the customer didn't reach max orders
        const vector<int> & Customer::getOrdersIds() const{return ordersId;}
        int Customer::addOrder(int _orderId){  //return OrderId if order was added successfully, -1 otherwise
            if(canMakeOrder()){
                ordersId.push_back(_orderId);
                return _orderId;
            } else return -1;}
        
//SoldierCustomer constructor
        SoldierCustomer::SoldierCustomer(int id, const string &name, int locationDistance, int maxOrders): Customer(id,name,locationDistance,maxOrders){};
//SoldierCustomer COPY constructor
        SoldierCustomer::SoldierCustomer(const SoldierCustomer& other):Customer(other){};
//SoldierCustomer destractor
        SoldierCustomer::~SoldierCustomer(){};
//SoldierCustomer methods
        SoldierCustomer* SoldierCustomer::clone() const {
            return new SoldierCustomer(*this);};
  
//CivilianCustomer constructor
        CivilianCustomer::CivilianCustomer(int id, const string &name, int locationDistance, int maxOrders): Customer( id, name, locationDistance, maxOrders){};
//CivilianCustomer COPY constructor
        CivilianCustomer::CivilianCustomer (const CivilianCustomer& other): Customer (other){};
//CivilianCustomer destractor
        CivilianCustomer::~CivilianCustomer(){};
//CivilianCustomer methods
        CivilianCustomer* CivilianCustomer::clone() const {
            return new CivilianCustomer(*this);};
    