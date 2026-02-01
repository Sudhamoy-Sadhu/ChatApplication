package com.example.chat.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.DTO.ForgotPassDTO;
import com.example.chat.DTO.ForgotPassOtpDTO;
import com.example.chat.Model.EmailOtpToken.OtpPurpose;
import com.example.chat.Service.OtpService;
import com.example.chat.Service.UserService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "${cors.allowed-origins}")
@RequestMapping("/forgot-password")
public class ForgotPassController {

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @PostMapping("/get-otp")
    public ResponseEntity<?> getOtp(@Valid @RequestBody ForgotPassOtpDTO forgotPassOtpDTO){
        try {
            otpService.sendOtp(forgotPassOtpDTO.getEmail(), OtpPurpose.PASSWORD_RESET);
            return ResponseEntity.ok("OTP Sent Successfully to your email");
        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ForgotPassDTO forgotPassDTO) {
        try {
            userService.changePassword(forgotPassDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("Password Changed Successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something Went wrong!" + e.getMessage());
        }
    }
}
