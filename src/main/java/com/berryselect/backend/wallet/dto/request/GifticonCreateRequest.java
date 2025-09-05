package com.berryselect.backend.wallet.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class GifticonCreateRequest {
        private Long productId;  // 기프티콘 상품 ID
        private String barcode;  // 바코드 값
        private Integer balance;  // 잔액

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate expiresAt;  // 만료일 (yyyy-MM-dd)

        @JsonIgnore
        private MultipartFile image;  // Json 역직렬화 무시(멀티파트에서만 바인딩)
}
