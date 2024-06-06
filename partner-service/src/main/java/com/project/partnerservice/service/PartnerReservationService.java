package com.project.partnerservice.service;


import com.project.domain.dto.ReservationDto;

import java.time.LocalDate;
import java.util.List;

public interface PartnerReservationService {

    List<ReservationDto> getReservationTimeTable(Long userId, LocalDate date);

    ReservationDto confirmReservation(Long userId, Long reservationId);

    ReservationDto declineReservation(Long userId, Long reservationId);

}
