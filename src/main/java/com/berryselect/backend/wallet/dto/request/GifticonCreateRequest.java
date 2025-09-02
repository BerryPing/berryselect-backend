package com.berryselect.backend.wallet.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class GifticonCreateRequest {
        private Long productId;  // 기프티콘 상품 ID
        private String barcode;  // 바코드 값
        private Integer balance;  // 잔액
        private String expiresAt;  // 만료일 (yyyy-MM-dd)
        @JsonIgnore private MultipartFile image;  // Json 역직렬화 무시(멀티파트에서만 바인딩)
}
