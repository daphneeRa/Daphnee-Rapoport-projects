#include <string>
#include <iostream>
#include <iostream>
#include <vector>
#include <sstream>
using std::string;
using std::vector;
#include "Order.h"

Order::Order(int _id, int _customerId, int _distance): id(_id), customerId(_customerId), distance(_distance), status(OrderStatus(0)), collectorId(NO_VOLUNTEER), driverId(NO_VOLUNTEER) {};
int Order::getId() const {return id;};
int Order::getCustomerId() const {return customerId;};
void Order::setStatus(OrderStatus _status) {status = _status;};
void Order::setCollectorId(int _collectorId) {collectorId = _collectorId;};
void Order::setDriverId(int _driverId) {driverId = _driverId;};
int Order::getCollectorId() const {return collectorId;};
int Order::getDriverId() const {return driverId;};
OrderStatus Order::getStatus() const {return status;};
const string Order::toString() const {
    std::string log;
    log.append("OrderId: " + std::to_string(id) + "\n");
    if(status == OrderStatus::PENDING){
            log.append("OrderStatus: Pending\n");}
    else if(status == OrderStatus::COLLECTING){
            log.append("OrderStatus: Collecting\n");}
    else if(status == OrderStatus::DELIVERING){
            log.append("OrderStatus: Delivering\n");}
    else if(status == OrderStatus::COMPLETED){
            log.append("OrderStatus: Completed\n");}   
    log.append("CustomerId: " + std::to_string(customerId) + "\n");
    if(collectorId == -1){
        log.append("Collector: None\n");
    }
    else {
        log.append("Collector: " + std::to_string(collectorId) + "\n"); 
    }
    if(driverId == -1){
        log.append("Driver: None\n");
    }
    else {  
        log.append("Driver: " + std::to_string(driverId) + "\n");
    }
    return log;
};

int Order::getDistance() const {
    return distance;
};