# CAR_RENTAL_SYSTEM
This is the simple car rental system API which allows customer to rent car for
specific dates and that different rates. The system can add Customers,cars,
reservation, update rates and calculate amount dues for each rental.
Setup
1.
2.
3.
4.
Clone this repository to your local machine.
Import the Project into your Preferred IDE.
And go the application.properties file to modify your database.
Build and run the Project.
Usage
The system provides the following endpoints:
Add Customers
Enpoint : POST /customer
Add a new customer to the system.
Request body:
Json file
{
“name” : “John”,
“phone” : “90887654321”
}
Add Car
Enpoint : POST /car
Add a new car to the system.
Request body:
Json file
{
“model” : “Tesla Model S”,
“year” : 2022,
“type” : “medium”,
“dailyRate” : 200.00,
“weeklyRate” : 1000.00}
Book Car
Enpoint : POST /reservatioin
Add a new reservation to the system.
Request body:
Json file
{
“rentalType” : “daily”,
“carType” : “medium”,
“customerId” : 1,
“startDate” : “2023-05-01”,
“endDate” : “2023-05-05”
}
Return Car
Enpoint : POST /return
Marks a rental a returned and calculates the amount due.
Request Parameters:
Param
{
“rentalID” : 1
}
Update Car Pricing
Enpoint : POST /rates
Updates the daily and / or weekly rates for a car type.
Request Parameters:
Param
{
“carType” : “medium”,
“dailyRate” : 250.00,
“weeklyRate” : 1200.00
}
