package com.music.Emotion.service.relationship;

import com.music.Emotion.model.entity.Album;
import com.music.Emotion.model.entity.Role;
import com.music.Emotion.repository.AlbumRepository;
import com.music.Emotion.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AlbumRoleService {

    private final AlbumRepository albumRepository;
    private final RoleRepository roleRepository;

    public Set<Role> getRoleByIds(Set<Integer> roleIds) {
        return new HashSet<>(roleRepository.findAllById(roleIds));
    }

    public Set<Album> getAlbumsByIds(Set<Integer> albumIds) {
        return new HashSet<>(albumRepository.findAllById(albumIds));
    }
}
