package com.huang.decorationsharingapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long totalMaterials;
    private long pendingMaterials;
    private long activeUsers;
    private double userGrowth;
    private double materialGrowth;
}