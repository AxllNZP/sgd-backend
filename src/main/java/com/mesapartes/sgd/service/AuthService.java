package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.LoginRequestDTO;
import com.mesapartes.sgd.dto.LoginResponseDTO;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO request);
}