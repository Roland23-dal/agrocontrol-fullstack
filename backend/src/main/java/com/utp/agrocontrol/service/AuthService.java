package com.utp.agrocontrol.service;

import com.utp.agrocontrol.dto.AuthResponseDTO;
import com.utp.agrocontrol.dto.LoginRequestDTO;
import com.utp.agrocontrol.dto.RegisterRequestDTO;

public interface AuthService {
    AuthResponseDTO registrar(RegisterRequestDTO dto);
    AuthResponseDTO login(LoginRequestDTO dto);
}
