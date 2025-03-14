package com.packshop.api.modules.shopping.payment.utility;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.packshop.api.modules.shopping.payment.service.MoMoPaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoMoSignatureGenerator {

  @Value("${momo.partner-code}")
  private String partnerCode;

  @Value("${momo.access-key}")
  private String accessKey;

  @Value("${momo.secret-key}")
  private String secretKey;

  private static final String MOMO_PARTNER_CODE = "MOMOBKUN20180529";
  private static final String MOMO_ACCESS_KEY = "klm05TvNBzhg7h7j";
  private static final String MOMO_SECRET_KEY = "at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa";
  private static final String MOMO_API_ENDPOINT = "https://test-payment.momo.vn/v2/gateway/api/create";

  public String generateSignature(String requestId, String orderId, Long amount) {
    String extraData = ""; // Phải khớp với requestBody
    String rawData = "accessKey=" + MOMO_ACCESS_KEY +
        "&amount=" + amount +
        "&extraData=" + extraData +
        "&ipnUrl=" + "http://localhost:8080/payment/ipn" + // Thêm ipnUrl
        "&orderId=" + orderId +
        "&orderInfo=" + "Payment for order #" + orderId.split("_")[1] + " (Sandbox Test)" + // Tạm dùng split để
                                                                                            // lấy ID
        "&partnerCode=" + MOMO_PARTNER_CODE +
        "&redirectUrl=" + "http://localhost:8080/payment/return" +
        "&requestId=" + requestId +
        "&requestType=captureWallet";

    log.info("Signature Raw Data: {}", rawData); // Log để kiểm tra

    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKeySpec = new SecretKeySpec(MOMO_SECRET_KEY.getBytes(StandardCharsets.UTF_8),
          "HmacSHA256");
      mac.init(secretKeySpec);
      byte[] hmacBytes = mac.doFinal(rawData.getBytes(StandardCharsets.UTF_8));
      return bytesToHex(hmacBytes);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException("Failed to generate HMAC-SHA256 signature", e);
    }
  }

  // Hàm phụ để chuyển byte[] thành chuỗi hex
  private static String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }
}