package com.carRentalSystem.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.carRentalSystem.modal.Car;
import com.carRentalSystem.modal.Customer;
import com.carRentalSystem.modal.Rental;

@RestController
public class CarRentalController {
	
	@Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/customer")
    public String addCustomer(@RequestBody Customer customer)throws Exception{
        try {
            String sql = "INSERT INTO Customer(Name, Phone) VALUES ( ?, ?)";
            jdbcTemplate.update(sql, customer.getName(), customer.getPhone());
            return "Customer added successfully";
        } catch (Exception e) {
        	e.printStackTrace();
            return "There is an error to Add the Customer";
        }
    }

    @PostMapping("/car")
    public String addCar(@RequestBody Car car) {
        try {
            String sql = "INSERT INTO Car(Model, Year, Type, Category, DailyRate, WeeklyRate) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, car.getModel(), car.getYear(), car.getType(),
                    car.getCategory(), car.getDailyRate(), car.getWeeklyRate());
            return "Car added successfully";
        } catch (Exception e) {
        	e.printStackTrace();
            return "There is an error to Add the Car";
        }
    }

    @PostMapping("/reservation")
    public String addReservation(@RequestBody Rental rental) {
        try {
            String sql = "SELECT VehicleID FROM Car WHERE Type = ? " +
                    "AND NOT EXISTS (SELECT * FROM Rental WHERE CarID = VehicleID " +
                    "AND ((StartDate >= ? AND StartDate <= ?) OR (EndDate >= ? AND EndDate <= ?))) " +
                    "LIMIT 1";
            Integer vehicleID = jdbcTemplate.queryForObject(sql, Integer.class, rental.getCarType(),
                    rental.getStartDate(), rental.getEndDate(), rental.getStartDate(), rental.getEndDate());
            if (vehicleID == null) {
                return "No available cars of the requested type for the requested period";
            }
            sql = "INSERT INTO Rental(RentalType, CarID, CustomerID, StartDate, EndDate, AmountDue) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            BigDecimal amountDue = calculateAmountDue(rental.getStartDate(), rental.getEndDate(), vehicleID);
            jdbcTemplate.update(sql, rental.getRentalType(), vehicleID, rental.getCustomerId(),
                    rental.getStartDate(), rental.getEndDate(), amountDue);
            return "Reservation added successfully. Amount due: " + amountDue.toString();
        } catch (Exception e) {
        	e.printStackTrace();
            return "There is an error in Booking";
        }
    }

    @PostMapping("/return")
    public String returnCar(@RequestParam Integer rentalID) {
        try {
            String sql = "SELECT CarID, StartDate, EndDate, DailyRate, WeeklyRate FROM Rental JOIN Car ON VehicleID = CarID WHERE RentalID = ?";
            Rental rental = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Rental.class), rentalID);
            BigDecimal amountDue = calculateAmountDue(rental.getStartDate(), rental.getEndDate(), rental.getCarID());
            sql = "UPDATE Rental SET AmountDue = ? WHERE RentalID = ?";
            jdbcTemplate.update(sql, amountDue, rentalID);
            return "Car returned successfully. Amount due: " + amountDue.toString();
        } catch (Exception e) {
        	e.printStackTrace();
            return "There is an error in returning the car";
        }
    }

    @PostMapping("/rates")
    public String updateRates(@RequestParam String carType,
                                          @RequestParam(required = false) BigDecimal dailyRate,
                                          @RequestParam(required = false) BigDecimal weeklyRate) {
    	try {
            String sql;
            if (dailyRate != null && weeklyRate != null) {
                sql = "UPDATE Car SET DailyRate = ?, WeeklyRate = ? WHERE Type = ?";
                jdbcTemplate.update(sql, dailyRate, weeklyRate, carType);
            } else if (dailyRate != null) {
                sql = "UPDATE Car SET DailyRate = ? WHERE Type = ?";
                jdbcTemplate.update(sql, dailyRate, carType);
            } else if (weeklyRate != null) {
                sql = "UPDATE Car SET WeeklyRate = ? WHERE Type = ?";
                jdbcTemplate.update(sql, weeklyRate, carType);
            } else {
                return "At least one of dailyRate or weeklyRate must be provide";
            }
            return "Rates updated successfully";
        } catch (Exception e) {
        	e.printStackTrace();
            return "There is an error in updating the car rental rates";
        }
    }
    
    
    private BigDecimal calculateAmountDue(LocalDate startDate, LocalDate endDate, int vehicleID) {
        String sql = "SELECT DailyRate, WeeklyRate FROM Car WHERE VehicleID = ?";
        Map<String, Object> car = jdbcTemplate.queryForMap(sql, vehicleID);
        BigDecimal dailyRate = (BigDecimal) car.get("DailyRate");
        BigDecimal weeklyRate = (BigDecimal) car.get("WeeklyRate");
        int days = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        int weeks = days / 7;
        int remainingDays = days % 7;
        BigDecimal amountDue = BigDecimal.ZERO;
        if (weeks > 0) {
            amountDue = amountDue.add(weeklyRate.multiply(BigDecimal.valueOf(weeks)));
        }
        if (remainingDays > 0) {
            amountDue = amountDue.add(dailyRate.multiply(BigDecimal.valueOf(remainingDays)));
        }
        return amountDue;
    }
	
}
