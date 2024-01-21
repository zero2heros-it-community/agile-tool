package org.zero2hero.applicationservice.service;


import org.zero2hero.applicationservice.dto.WorkspaceCreateDto;
import org.zero2hero.applicationservice.dto.WorkspaceViewDto;
import org.zero2hero.applicationservice.entity.Workspace;

public interface WorkspaceService {
    WorkspaceViewDto create(WorkspaceCreateDto workspaceCreateDto);

    Workspace findWorkspaceById(Long id);

    void deleteWorkspaceById(Long id);
}