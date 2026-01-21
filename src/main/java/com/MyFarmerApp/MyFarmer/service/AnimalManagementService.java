package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.dto.AnimalManagementRequest;
import com.MyFarmerApp.MyFarmer.dto.AnimalManagementResponse;

import java.util.List;

public interface AnimalManagementService {

    AnimalManagementResponse addRecord(AnimalManagementRequest request);

    AnimalManagementResponse updateRecord(Long id, AnimalManagementRequest request);

    List<AnimalManagementResponse> getHistoryByCattle(Long cattleId);

    AnimalManagementResponse getLatestRecord(Long cattleId);

    void deleteRecord(Long id);
}
