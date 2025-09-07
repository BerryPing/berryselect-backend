package com.berryselect.backend.wallet.dto.response;

import com.berryselect.backend.wallet.domain.type.GifticonStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GifticonSummaryResponse {
    private String type;
    private List<GifticonSummary> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GifticonSummary {
        private Long id;
        private String name;
        private String barcode;
        private Integer balance;
        private String expiresAt;
        private GifticonStatus status; // ACTIVE/USED/EXPIRED
    }
}