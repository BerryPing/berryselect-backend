package com.berryselect.backend.wallet.dto.response;

import com.berryselect.backend.wallet.domain.type.GifticonStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GifticonResponse {
    private Long id;
    private String type;          // "GIFTICON"
    private String name;
    private String barcode;
    private Integer balance;
    private String expiresAt;
    private GifticonStatus gifticonStatus; // ACTIVE/USED/EXPIRED
}