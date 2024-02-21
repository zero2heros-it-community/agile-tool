package org.zero2hero.applicationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.zero2hero.applicationservice.dto.WorkspaceCreateDto;
import org.zero2hero.applicationservice.dto.WorkspaceViewDto;
import org.zero2hero.applicationservice.entity.Workspace;
import org.zero2hero.applicationservice.exception.AlreadyExistException;
import org.zero2hero.applicationservice.exception.IdFormatException;
import org.zero2hero.applicationservice.exception.NameFormatException;
import org.zero2hero.applicationservice.exception.NotFoundException;
import org.zero2hero.applicationservice.repository.WorkspaceRepository;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class WorkspaceServiceImp implements WorkspaceService {
    private final KafkaTemplate kafkaTemplate;
    private final WorkspaceRepository workspaceRepository;

    @Autowired
    public WorkspaceServiceImp(KafkaTemplate kafkaTemplate, WorkspaceRepository workspaceRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.workspaceRepository = workspaceRepository;
    }


    @Override
    public WorkspaceViewDto create(WorkspaceCreateDto workspaceCreateDto) {

        if (!isNameRightFormat(workspaceCreateDto.getName())) {
            throw new NameFormatException("workspace name is in incorrect format");
        }
        if(workspaceRepository.findByName(workspaceCreateDto.getName()).isPresent()){
            throw new AlreadyExistException(" workspace is already exist");
        }

        Workspace workspace = new Workspace();
        workspace.setName(workspaceCreateDto.getName());
        workspace = workspaceRepository.save(workspace);
        System.out.println("Workspace" + workspace);
        this.kafkaTemplate.send("first_topic", "user-key", workspace);
        return WorkspaceViewDto.of(workspace);
    }

    public Workspace findWorkspaceById(Long id) {
        if (id == null || id <= 0) {
            throw new IdFormatException("Workspace ID is in incorrect format");
        }
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Workspace not found with ID: " + id));
        return workspace;
    }

    @Override
    public List<Workspace> getAll() {
        return workspaceRepository.findAll();
    }

    private boolean isNameRightFormat(String name) {
        Pattern pattern = Pattern.compile("^[a-z]+$");
        return pattern.matcher(name).matches();
    }

    @Override
    public WorkspaceViewDto updateOneWorkspace (Long id, WorkspaceCreateDto workspaceCreateDto){
        Workspace existingWorkspace = workspaceRepository.findById(id).
                orElseThrow(() -> new NameFormatException(("Workspace not found with ID: " + id)));

        if(!isNameRightFormat(workspaceCreateDto.getName())){
            throw new NameFormatException("Workspace name is in incorrect format");
        }

        existingWorkspace.setName(workspaceCreateDto.getName());
        Workspace updatedOneWorkspace = workspaceRepository.save(existingWorkspace);

        this.kafkaTemplate.send("first_topic", "user-key", updatedOneWorkspace);
        return WorkspaceViewDto.of(updatedOneWorkspace);

    }
}
