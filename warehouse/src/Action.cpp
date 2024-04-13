#include <string>
#include <vector>
#include <iostream>
#include <sstream>
#include "WareHouse.h"
using std::string;
using std::vector;
#include "Action.h"
#include "Volunteer.h"
#include "Customer.h"
using namespace std;

extern WareHouse* backup;

//BaseAction Constructor
    BaseAction::BaseAction(): errorMsg(), status(){};
//BaseAction destractor
    BaseAction::~BaseAction(){};
//BaseAction Methods
    ActionStatus BaseAction::getStatus() const {return status;};
    void BaseAction::complete() {status = ActionStatus::COMPLETED;};
    void BaseAction::error(string _errorMsg) {
        status = ActionStatus::ERROR;
        errorMsg = _errorMsg;
        cout << errorMsg << endl;
    }
    string BaseAction::getErrorMsg() const {return errorMsg;};
    int BaseAction::traslateCustomerType(string customerType) {
        if(customerType == "soldier"){
            return 0;
        }
        else {
            return 1;
        }
    }

//SimulateStep Constructor
    SimulateStep::SimulateStep(int _numOfSteps): BaseAction(), numOfSteps(_numOfSteps) {};
//SimulateStep destractor
    SimulateStep::~SimulateStep(){};
//SimulateStep Methods
    void SimulateStep::act(WareHouse &wareHouse) {
        for (int i = 1;i<=numOfSteps;i++){
            //part 1 
            for (Order* order : wareHouse.getPendingOrders()){
                if(order->getStatus() == OrderStatus::PENDING){ //The order need Collector
                    for (Volunteer* volunteer : wareHouse.getVolunteers()){
                        if(volunteer->getType()==0 && volunteer->canTakeOrder(*order)){
                            volunteer->acceptOrder(*order);
                            order->setStatus(OrderStatus(1));
                            order->setCollectorId(volunteer->getId());
                            wareHouse.addOrder(order);
                            break;
                        }
                    }
                }
                else if(order->getStatus() == OrderStatus::COLLECTING){//The order need Driver
                    for (Volunteer* volunteer : wareHouse.getVolunteers()){
                        if(volunteer->getType()==1 && volunteer->canTakeOrder(*order)){
                            volunteer->acceptOrder(*order);
                            order->setStatus(OrderStatus(2));
                            order->setDriverId(volunteer->getId());
                            wareHouse.addOrder(order);
                            break;
                        }
                    }
                }
            }
            //part 2
            for (Volunteer* volunteer : wareHouse.getVolunteers()){
                if(volunteer->isBusy()==true){
                    volunteer->step();
                }
            }
            //part 3 
            for (Volunteer* volunteer : wareHouse.getVolunteers()){
                if(volunteer->isBusy()==false && (volunteer->getCompletedOrderId())!=NO_ORDER){
                    if (volunteer->getType()==0){ //is collector
                        wareHouse.addOrder(&wareHouse.getOrder(volunteer->getCompletedOrderId()));
                        volunteer->resetCompletedOrderId();
                    }else{ //is driver 
                        (wareHouse.getOrder(volunteer->getCompletedOrderId())).setStatus(OrderStatus(3));
                        wareHouse.addOrder(&wareHouse.getOrder(volunteer->getCompletedOrderId()));
                    }
                    //part 4
                    if (!volunteer->hasOrdersLeft()){
                        wareHouse.deleteVolunteer(volunteer->getId());
                    }
                }
            }
        }
        complete();
        wareHouse.addAction(this);
    }

    std::string SimulateStep::toString() const {
    std::string log;
    log.append("simulateStep " + to_string(numOfSteps));
    if(getStatus() == ActionStatus::COMPLETED ){
        log.append(" COMPLETED\n");}
    else{
        log.append(" ERROR\n");
    }
    return log;
    }
    SimulateStep* SimulateStep::clone() const {
        return new SimulateStep(*this); 
    };

//AddOrder Constructor
    AddOrder::AddOrder(int id): BaseAction(), customerId(id){};
//AddOrder destractor
    AddOrder::~AddOrder(){};
//AddOrder Methods
    void AddOrder::act(WareHouse &wareHouse) {
        if((customerId >= wareHouse.getCustomerCounter()) || (!wareHouse.getCustomer(customerId).canMakeOrder())){
            error("Cannot place this order");
        }
        else{
            int distance = wareHouse.getCustomer(customerId).getCustomerDistance();
            Order* order = new Order( wareHouse.getOrderCounter(), customerId, distance);
            wareHouse.addOrder(order);
            complete();
        }
        wareHouse.addAction(this); 
    }
    string AddOrder::toString() const {
        std::string log;
        log.append("order " + to_string(customerId));
        if(getStatus() == ActionStatus::COMPLETED){
            log.append(" COMPLETED\n");}
        else{
            log.append(" ERROR\n");
        }
        return log;
    }
    AddOrder* AddOrder::clone() const {
        return new AddOrder(*this); 
    }

//AddCustomer constructor 
    AddCustomer::AddCustomer(string _customerName, string _customerType, int _distance, int _maxOrders): BaseAction(), customerName(_customerName), customerType(CustomerType(traslateCustomerType(_customerType))), distance(_distance), maxOrders(_maxOrders) {};
//AddCustomer destractor
    AddCustomer::~AddCustomer(){};
//methods
    void AddCustomer::act(WareHouse &wareHouse) {
        if(customerType == CustomerType::Civilian){
            SoldierCustomer* customer = new SoldierCustomer(wareHouse.getCustomerCounter(), customerName, distance, maxOrders);
            wareHouse.addCustomer(customer);
        }
        else {
            CivilianCustomer* customer = new CivilianCustomer(wareHouse.getCustomerCounter(), customerName, distance, maxOrders);
            wareHouse.addCustomer(customer);
        }
        complete();
        wareHouse.addAction(this); 
    }
    AddCustomer* AddCustomer::clone() const {
        return new AddCustomer(*this); 
    }
    string AddCustomer::toString() const {
        std::string log;
        if(customerType == CustomerType::Civilian ){
            log.append("customer " + customerName + " Soldier " + std::to_string(distance) + " " + std::to_string(maxOrders));
        } else {
            log.append("customer " + customerName + " Civilian " + std::to_string(distance) + " " + std::to_string(maxOrders));
        } if(getStatus() == ActionStatus::COMPLETED){
            log.append(" COMPLETED\n");
        } else{
            log.append(" ERROR\n");
        } return log;
    }
// PrintOrderStatus constructor
    PrintOrderStatus::PrintOrderStatus(int id): BaseAction(), orderId(id) {};
//PrintOrderStatus destractor
    PrintOrderStatus::~PrintOrderStatus(){};
//PrintOrderStatus methods
    void PrintOrderStatus::act(WareHouse &wareHouse) {
        if(orderId >= wareHouse.getOrderCounter()){
            error("Order doesn't exist\n");
        } else {
            Order order = wareHouse.getOrder(orderId);
            std::string str = order.toString();
            std::cout << str << std::endl;
            complete();
        }
        wareHouse.addAction(this); 
    }
    PrintOrderStatus* PrintOrderStatus::clone() const {
        return new PrintOrderStatus(*this); 
    }
    string PrintOrderStatus::toString() const {
        std::string log;
        log.append("orderStatus " + to_string(orderId)); 
        if(getStatus() == ActionStatus::COMPLETED){
            log.append(" COMPLETED\n");}
        else{
            log.append(" ERROR\n");
        }
        return log;
    }
//PrintCustomerStatus constructor 
    PrintCustomerStatus::PrintCustomerStatus(int _customerId): BaseAction(), customerId(_customerId) {};
//PrintCustomerStatus destractor
    PrintCustomerStatus::~PrintCustomerStatus(){};
//PrintCustomerStatus methods
    void PrintCustomerStatus::act(WareHouse &wareHouse) {
        if(customerId >= wareHouse.getCustomerCounter()){
            error("Customer doesn't exist\n");
        }else {
            std::cout << "CustomerId: " + std::to_string(customerId) << std::endl;
            for(int order : wareHouse.getCustomer(customerId).getOrdersIds()){
                std::cout << "OrderId: " + std::to_string(order) << std::endl;
                OrderStatus status = wareHouse.getOrder(order).getStatus();
                if(status == OrderStatus::PENDING){
                    std::cout << "OrderStatus: Pending" << std::endl ;}
                else if(status == OrderStatus::COLLECTING){
                    std::cout << "OrderStatus: Collecting" << std::endl ;}
                else if(status == OrderStatus::DELIVERING){
                    std::cout << "OrderStatus: Delivering" << std::endl;}
                else if(status == OrderStatus::COMPLETED){
                    std::cout << "OrderStatus: Completed" << std::endl;}   
            }
            std::cout << "numOrdersLeft: " << (to_string(wareHouse.getCustomer(customerId).getMaxOrders() - wareHouse.getCustomer(customerId).getNumOrders())) << std::endl;
            complete();
        }
        wareHouse.addAction(this);  
    }    
    PrintCustomerStatus* PrintCustomerStatus::clone() const {
        return new PrintCustomerStatus(*this); 
    }
    string PrintCustomerStatus::toString() const {
        std::string log;
        log.append("customerStatus " + to_string(customerId));
        if(getStatus() == ActionStatus::COMPLETED){
            log.append(" COMPLETED\n");
        } else{ 
            log.append(" ERROR\n");
        }
        return log;
    }

//PrintVolunteerStatus constructor
    PrintVolunteerStatus::PrintVolunteerStatus(int id): BaseAction(), VolunteerId(id) {};
//PrintVolunteerStatus distructor 
    PrintVolunteerStatus::~PrintVolunteerStatus(){};
//PrintVolunteerStatus methods
    void PrintVolunteerStatus::act(WareHouse &wareHouse) {
        if(!wareHouse.isVolunteerExist(VolunteerId)){
            error("Volunteer doesn't exist");
        }else{
            std::string str = wareHouse.getVolunteer(VolunteerId).toString();
            std::cout << str << std::endl;
            complete();
        }
        wareHouse.addAction(this); 
    }
    PrintVolunteerStatus* PrintVolunteerStatus::clone() const {
        return new PrintVolunteerStatus(*this); 
    }
    string PrintVolunteerStatus::toString() const {
        std::string log;
        log.append("volunteerStatus " + to_string(VolunteerId));
        if(getStatus() == ActionStatus::COMPLETED){
            log.append(" COMPLETED\n");
        }else{
            log.append(" ERROR\n");
        }
        return log;
    }
    
// PrintActionsLog constructor 
    PrintActionsLog::PrintActionsLog(): BaseAction() {};
//PrintActionsLog destractor
     PrintActionsLog::~PrintActionsLog(){};
//PrintActionsLog methods
    void PrintActionsLog::act(WareHouse &wareHouse) {
        vector<BaseAction*> actions = wareHouse.getActions();
        std::string log;
        for(auto action : actions){ 
            std::cout << action->toString() << std::endl;
        }
        complete();
        wareHouse.addAction(this); 
    }
    PrintActionsLog* PrintActionsLog::clone() const {
        return new PrintActionsLog(*this); 
    }
    string PrintActionsLog::toString() const {
        std::string log;
        log.append("log ");
        if(getStatus() == ActionStatus::COMPLETED){
            log.append(" COMPLETED\n");
        }else{
            log.append(" ERROR\n");
        }
        return log;
    }

// Close constructor
    Close::Close(): BaseAction(){};
//Close destractor
    Close::~Close(){};
//Close methods
    void Close::act(WareHouse &wareHouse) {
        for(Order* order : wareHouse.getPendingOrders()){
            std::cout << statusLog(order) << std::endl;
        }
        for(Order* order : wareHouse.getInProcessOrders()){
            std::cout << statusLog(order) << std::endl;
        }
        for(Order* order : wareHouse.getCompletedOrders()){
            std::cout << statusLog(order) << std::endl;
        }
        wareHouse.close();
        complete();
        wareHouse.addAction(this);
    };

    string Close::statusLog(Order* order){ 
        std::string log;
        log.append("OrderID: " + to_string(order->getId()));
        log.append(" , CustomerID: " + to_string(order->getCustomerId()));
        if(order->getStatus() == OrderStatus::PENDING){
            log.append(" , OrderStatus: Pending\n");}
        else if(order->getStatus() == OrderStatus::COLLECTING){
            log.append(" , OrderStatus: Collecting\n");}
        else if(order->getStatus() == OrderStatus::DELIVERING){
            log.append(" , OrderStatus: Delivering\n");}
        else if(order->getStatus() == OrderStatus::COMPLETED){
            log.append(" , OrderStatus: Completed\n");}   
        return log;
    }

    Close* Close::clone() const { 
        return new Close(*this);};
    string Close::toString() const {
        std::string log;
        log.append("close ");
        if(getStatus() == ActionStatus::COMPLETED){
            log.append(" COMPLETED\n");
        }else{
            log.append(" ERROR\n");
        }
        return log;
    };

//BackWareHouse constructor
    BackupWareHouse::BackupWareHouse(): BaseAction(){};
//BackWareHouse destractor
    BackupWareHouse::~BackupWareHouse(){};
//BackWareHouse methods
    void BackupWareHouse::act(WareHouse &wareHouse) {
        wareHouse.backupFunc();
        complete();
        wareHouse.addAction(this);
    };
    BackupWareHouse *BackupWareHouse::clone() const {
        return new BackupWareHouse(*this);
    };
    string BackupWareHouse::toString() const {
        std::string log;
        log.append("backup ");
        if(getStatus() == ActionStatus::COMPLETED){
            log.append(" COMPLETED\n");
        }else{
            log.append(" ERROR\n");
        }
        return log;
    };

//RestoreWareHouse constructor
    RestoreWareHouse::RestoreWareHouse(): BaseAction() {};
//RestoreWareHouse destractor
    RestoreWareHouse::~RestoreWareHouse(){};
//RestoreWareHouse methods
    void RestoreWareHouse::act(WareHouse &wareHouse) {
        if(backup == nullptr){
            error("No backup available");
        }
        else{
            wareHouse.restore();
            complete();
            wareHouse.addAction(this);
        }
    };
    RestoreWareHouse *RestoreWareHouse::clone() const {
        return new RestoreWareHouse(*this);
    };
    string RestoreWareHouse::toString() const {
        std::string log;
            log.append("restore ");
            if(getStatus() == ActionStatus::COMPLETED){
                log.append(" COMPLETED\n");
            }else{
                log.append(" ERROR\n");
            }
            return log;
    };