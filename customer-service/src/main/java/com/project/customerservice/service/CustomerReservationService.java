package com.project.customerservice.service;

import com.project.customerservice.model.CustomerReservationForm;
import com.project.domain.response.ReservationResponse;

public interface CustomerReservationService {

    ReservationResponse makeReservation(Long userId, CustomerReservationForm form);

}
