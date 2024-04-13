#include "Volunteer.h"
#include <string>
#include <vector>
#include "Order.h"
#include <algorithm>
using std::string;
using std::vector;

//Volunteer constractor
        Volunteer::Volunteer(int _id, const string &_name) : completedOrderId(NO_ORDER), activeOrderId(NO_ORDER), volunteerType(), id(_id), name(_name){};
//Volunteer COPY constractor
        Volunteer::Volunteer(const Volunteer& other):completedOrderId(other.completedOrderId), activeOrderId(other.activeOrderId), volunteerType(other.volunteerType), id(other.id), name(other.name){};
//Volenteer destractor
        Volunteer::~Volunteer(){};
//Volunteer methods
        int Volunteer::getId() const {return id;}
        const string& Volunteer::getName() const {return name;}
        const int Volunteer::getType() const {return volunteerType;}
        int Volunteer::getActiveOrderId() const{return activeOrderId;}
        int Volunteer::getCompletedOrderId() const {return completedOrderId;}
        bool Volunteer::isBusy() const{ return activeOrderId!=NO_ORDER;}; // Signal whether the volunteer is currently processing an order
        void Volunteer::resetCompletedOrderId(){completedOrderId=NO_ORDER;};

//CollectorVolunteer constractor
        CollectorVolunteer::CollectorVolunteer(int _id, const string &_name, int _coolDown): Volunteer(_id, _name), coolDown(_coolDown), timeLeft(0){volunteerType=0;};
//CollectorVolunteer COPY constractor
        CollectorVolunteer::CollectorVolunteer(const CollectorVolunteer& other):Volunteer(other), coolDown(other.coolDown), timeLeft(other.timeLeft){volunteerType=other.volunteerType;};
//CollectorVolunteer destractor
        CollectorVolunteer::~CollectorVolunteer(){};
//CollectorVolunteer methods
        CollectorVolunteer* CollectorVolunteer::clone() const {
            return new CollectorVolunteer(*this);}
        void CollectorVolunteer::step() {
            if (decreaseCoolDown()){
            completedOrderId=activeOrderId;
            activeOrderId=NO_ORDER;}
        }
        int CollectorVolunteer::getCoolDown() const {return coolDown;}
        int CollectorVolunteer::getTimeLeft() const {return timeLeft;}
        bool CollectorVolunteer::decreaseCoolDown(){
            if ((timeLeft==0)|(timeLeft==1)){
                timeLeft=0;
                return true;}
            else {
                timeLeft--;
                return false;}}//Decrease timeLeft by 1,return true if timeLeft=0,false otherwise
        void CollectorVolunteer::setTimeLeft(int _timeLeft){timeLeft=_timeLeft;}
        bool CollectorVolunteer::hasOrdersLeft() const {return true;};
        bool CollectorVolunteer::canTakeOrder(const Order &order) const {return activeOrderId == NO_ORDER;};
        void CollectorVolunteer::acceptOrder(const Order &order){
            activeOrderId= order.getId();
            timeLeft=coolDown;
        }
        string CollectorVolunteer::toString() const {
            std::string log;
            log.append("VolunteerID: "+std::to_string(this->getId())+"\n"+"isBusy:  ");
            if (activeOrderId!=NO_ORDER){
                log.append("True\n" "OrderID:"+ std::to_string(activeOrderId)+"\n");}
                else {
                    log.append("False\n OrderID: None\n");}
            if (timeLeft!=0){log.append("TimeLeft: "+ std::to_string(timeLeft) + "\n");}
                else{
                    log.append("TimeLeft: None\n");}
            log.append("OrdersLeft: No Limit");        
            return log;
        }; 

// LimitedCollectorVolunteer constractor
        LimitedCollectorVolunteer::LimitedCollectorVolunteer(int _id, const string &_name, int _coolDown ,int _maxOrders): CollectorVolunteer(_id, _name,_coolDown), maxOrders(_maxOrders),ordersLeft(_maxOrders){};
//LimitedCollectorVolunteer COPY constractor 
        LimitedCollectorVolunteer::LimitedCollectorVolunteer(const LimitedCollectorVolunteer &other): CollectorVolunteer(other), maxOrders(other.maxOrders),ordersLeft(other.maxOrders){}
// LimitedCollectorVolunteer destractor 
        LimitedCollectorVolunteer::~LimitedCollectorVolunteer(){};
// LimitedCollectorVolunteer methods
        LimitedCollectorVolunteer* LimitedCollectorVolunteer::clone() const {
            return new LimitedCollectorVolunteer (*this);
        }
        bool LimitedCollectorVolunteer::hasOrdersLeft() const {return ordersLeft!=0;};
        bool LimitedCollectorVolunteer::canTakeOrder(const Order &order) const {return (activeOrderId==NO_ORDER && ordersLeft!=0);}
        void LimitedCollectorVolunteer::acceptOrder(const Order &order){
            activeOrderId=order.getId();
            this->setTimeLeft(this->getCoolDown());
            ordersLeft--;
        }
        int LimitedCollectorVolunteer::getMaxOrders() const{return maxOrders;};
        int LimitedCollectorVolunteer::getNumOrdersLeft() const{return ordersLeft;};
        string LimitedCollectorVolunteer::toString() const{
            std::string log;
            log.append("VolunteerID: "+std::to_string(this->getId())+"\n"+"isBusy:  ");
            if (activeOrderId!=NO_ORDER){log.append("True\n" "OrderID:"+ std::to_string(activeOrderId)+"\n" );}
                else {log.append("False\n OrderID: None\n");}
            if (this->getTimeLeft()!=0){log.append("TimeLeft: "+ std::to_string(this->getTimeLeft()) + "\n");}
                else{log.append("TimeLeft: None\n");}
            log.append("OrdersLeft: ");
            log.append(std::to_string(ordersLeft));
            return log;
        }


//DriverVolunteer cunstractor
        DriverVolunteer::DriverVolunteer(int _id, const string &_name, int _maxDistance, int _distancePerStep):Volunteer(_id,_name),maxDistance(_maxDistance),distancePerStep(_distancePerStep),distanceLeft(0){volunteerType=1;};
//DriverVolunteer COPY constractor 
        DriverVolunteer::DriverVolunteer(const DriverVolunteer &other):Volunteer(other),maxDistance(other.maxDistance),distancePerStep(other.distancePerStep),distanceLeft(other.distanceLeft){volunteerType=other.volunteerType;};
//DriverVolunteer destractor
        DriverVolunteer::~DriverVolunteer(){};
//DriverVolunteer method
        DriverVolunteer* DriverVolunteer::clone() const {
            return new DriverVolunteer(*this);
        }
        int DriverVolunteer::getDistanceLeft() const{return distanceLeft;}
        void DriverVolunteer::setDistanceLeft(int _distanceLeft){
            distanceLeft=_distanceLeft;
        }
        int DriverVolunteer::getMaxDistance() const{return maxDistance;}
        int DriverVolunteer::getDistancePerStep() const{return distancePerStep;}  
        bool DriverVolunteer::decreaseDistanceLeft() {
            if(distanceLeft-distancePerStep > 0){
                distanceLeft = distanceLeft-distancePerStep;
                return false;
            }
            else {
                distanceLeft = 0;
                return true;
            }
        } //Decrease distanceLeft by distancePerStep,return true if distanceLeft<=0,false otherwise
        bool DriverVolunteer::hasOrdersLeft() const {return true;}
        bool DriverVolunteer::canTakeOrder(const Order &order) const {
            if (activeOrderId==NO_ORDER && order.getDistance()<=maxDistance){return true;}
            else{return false;}
        } // Signal if the volunteer is not busy and the order is within the maxDistance
        void DriverVolunteer::acceptOrder(const Order &order) {
            activeOrderId=order.getId();
            distanceLeft = order.getDistance();}// Assign distanceLeft to order's distance
        void DriverVolunteer::step(){
            if(decreaseDistanceLeft()){
            completedOrderId=activeOrderId;
            activeOrderId=NO_ORDER;}
        } // Decrease distanceLeft by distancePerStep
        string DriverVolunteer::toString() const{
            std::string log;
            log.append("VolunteerID: "+std::to_string(this->getId())+"\n"+"isBusy:  ");
            if (activeOrderId!=NO_ORDER){log.append("True\n" "OrderID:"+ std::to_string(activeOrderId)+"\n" );}
                else {log.append("False\n OrderID: None\n");}
            if (distanceLeft!=0){log.append("TimeLeft: "+ std::to_string(distanceLeft) + "\n");}
                else{log.append("TimeLeft: None\n");}
            log.append("OrdersLeft: No Limit");
            return log;
        }

// LimitedDriverVolunteer cunstractor
        LimitedDriverVolunteer::LimitedDriverVolunteer(int _id, const string &_name, int _maxDistance, int _distancePerStep,int _maxOrders):DriverVolunteer(_id,_name,_maxDistance,_distancePerStep),maxOrders(_maxOrders),ordersLeft(maxOrders){};
//LimiktedDriverVolunteer COPT constractor 
        LimitedDriverVolunteer::LimitedDriverVolunteer(const LimitedDriverVolunteer &other):DriverVolunteer(other),maxOrders(other.maxOrders),ordersLeft(other.maxOrders){};
// LimitedDriverVolunteer destractor
        LimitedDriverVolunteer::~LimitedDriverVolunteer(){};
//LimitedDriverVolunteer methods
        LimitedDriverVolunteer* LimitedDriverVolunteer::clone() const{
            return new LimitedDriverVolunteer(*this); }
        int LimitedDriverVolunteer::getMaxOrders() const {return maxOrders;}
        int LimitedDriverVolunteer::getNumOrdersLeft() const {return ordersLeft;};
        bool LimitedDriverVolunteer::hasOrdersLeft() const {return ordersLeft!=0;}
        bool LimitedDriverVolunteer::canTakeOrder(const Order &order) const {
            if((activeOrderId==NO_ORDER) && (ordersLeft!=0) && (order.getDistance()<=(this->getMaxDistance())))
            {return true;} 
            else {return false;}
        }//Signal if the volunteer is not busy, the order is within the maxDistance.
        void LimitedDriverVolunteer::acceptOrder(const Order &order) {
            activeOrderId=order.getId();
            setDistanceLeft(order.getDistance());
            ordersLeft--;
        }// Assign distanceLeft to order's distance and decrease ordersLeft
        string LimitedDriverVolunteer::toString() const {
            std::string log;
            log.append("VolunteerID: "+std::to_string(this->getId())+"\n"+"isBusy:  ");
            if (activeOrderId!=NO_ORDER){log.append("True\n" "OrderID:"+ std::to_string(activeOrderId)+"\n" );}
                else {log.append("False\n OrderID: None\n");}
            if (getDistanceLeft()==0){log.append("TimeLeft: "+ std::to_string(getDistanceLeft()) + "\n");}
                else{log.append("TimeLeft: None\n");}
            log.append("OrdersLeft: " + std::to_string(ordersLeft));
            return log;
        }