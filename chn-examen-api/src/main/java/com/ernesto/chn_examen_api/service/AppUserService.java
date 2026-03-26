package com.ernesto.chn_examen_api.service;

import com.ernesto.chn_examen_api.dto.AppUserDTO;
import com.ernesto.chn_examen_api.dto.AppUserPatchDTO;

import java.util.List;

public interface AppUserService {

    AppUserDTO createUser(AppUserDTO appUserDTO);

    AppUserDTO getUserById(Integer id);

    List<AppUserDTO> getAllUsers();

    AppUserDTO updateUser(Integer id, AppUserDTO appUserDTO);

    AppUserDTO patchUser(Integer id, AppUserPatchDTO patchDTO);

    void deleteUser(Integer id);
}
