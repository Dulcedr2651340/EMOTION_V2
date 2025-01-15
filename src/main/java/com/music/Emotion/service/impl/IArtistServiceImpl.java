package com.music.Emotion.service.impl;

import com.music.Emotion.exception.ArtistNotFoundException;
import com.music.Emotion.mapper.ArtistMapper;
import com.music.Emotion.model.dto.ArtistRequest;
import com.music.Emotion.model.dto.ArtistResponse;
import com.music.Emotion.model.entity.*;
import com.music.Emotion.repository.*;
import com.music.Emotion.service.IArtistService;
import com.music.Emotion.service.relationship.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IArtistServiceImpl implements IArtistService {

    // Inyectamos el repositorio como una dependencia inmutable
    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    private final RoleRepository roleRepository;
    private final ArtistRepository artistRepository;
    private final ArtistMapper artistMapper;

    private final SongGenreService songGenreService;
    private final ArtistGenreService artistGenreService;
    private final ArtistAlbumService artistAlbumService;
    private final ArtistRoleService artistRoleService;
    private final ArtistSongService artistSongService;


    @Override
    public List<ArtistResponse> getAllArtists() {
        log.info("Fetching all artists"); // Inicializando el método
        return artistRepository.findAll()
                .stream()
                .filter(artist -> artist.getStatus()) //Filtrar solo los artistas activos
                .map(artistMapper::toArtistResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ArtistResponse saveArtist(ArtistRequest artistRequest) {
        log.info("Saving artist: {}", artistRequest); // Indicando que se está intentando guardar un artist;

        Artist artist = artistMapper.toEntity(artistRequest);
        log.debug("Mapped Artist entity: {}", artist); // Registro del artist mapeado desde ArtistRequest

        Set<Genre> genres = artistGenreService.getGenresByIds(artistRequest.getGenreIds());
        log.debug("Retrieved genres for artist: {}", genres); // Géneros recuperados por sus IDs

        Set<Album> albums = artistAlbumService.getAlbumByIds(artistRequest.getAlbumIds());
        log.debug("Retrieved albums for artist: {}", albums); // Album recuperados por sus IDs

        Set<Role>  roles  = artistRoleService.getRolesByIds(artistRequest.getRoleIds());
        log.debug("Retrieved role for artist: {}", roles); // roles recuperados por sus IDs

        Set<Song>  songs  = artistSongService.getSongsByIds(artistRequest.getSongIds());
        log.debug("Retrieved songs for artist: {}", songs); // Song recuperados por sus IDs

        artist.setGenres(genres);
        artist.setAlbums(albums);
        artist.setRoles(roles);
        artist.setSongs(songs);

        Artist savedArtist = artistRepository.save(artist);
        log.info("Artist saved successfully with ID: {}", savedArtist.getId()); // Indicando que el artist ha sido guardado exitosamente con su ID

        return artistMapper.toArtistResponse(savedArtist);

    }

    @Override
    public ArtistResponse updateArtist(Integer id, ArtistRequest artistRequest) {
        // Log de entrada al método
        log.info("Starting updateArtist with ID: {}", id);

        // 1. Recuperar la entidad original
        log.debug("Searching artist by ID: {}", id);
        Artist existingArtist = artistRepository.findById(id)
                .orElseThrow(() -> new ArtistNotFoundException("No artist found with ID: " + id));

        log.info("Artist found: {}", existingArtist.getName());

        // 2. Actualizar campos simples
        if (artistRequest.getName() != null) {
            log.debug("Updating artist name to: {}", artistRequest.getName());
            existingArtist.setName(artistRequest.getName());
        }
        if (artistRequest.getStageName() != null) {
            log.debug("Updating artist stageName to: {}", artistRequest.getStageName());
            existingArtist.setStageName(artistRequest.getStageName());
        }
        if (artistRequest.getType() != null) {
            log.debug("Updating artist type to: {}", artistRequest.getType());
            existingArtist.setType(artistRequest.getType());
        }
        if (artistRequest.getCountry() != null) {
            log.debug("Updating artist country to: {}", artistRequest.getCountry());
            existingArtist.setCountry(artistRequest.getCountry());
        }
        if (artistRequest.getDebutDate() != null) {
            log.debug("Updating artist debutDate to: {}", artistRequest.getDebutDate());
            existingArtist.setDebutDate(artistRequest.getDebutDate());
        }

        // 3. Agregar relaciones sin borrar lo existente
        if (artistRequest.getGenreIds() != null && !artistRequest.getGenreIds().isEmpty()) {
            log.debug("Adding genres with IDs: {}", artistRequest.getGenreIds());
            List<Genre> newGenres = genreRepository.findAllById(artistRequest.getGenreIds());
            existingArtist.getGenres().addAll(newGenres);
        }
        if (artistRequest.getAlbumIds() != null && !artistRequest.getAlbumIds().isEmpty()) {
            log.debug("Adding albums with IDs: {}", artistRequest.getAlbumIds());
            List<Album> newAlbums = albumRepository.findAllById(artistRequest.getAlbumIds());
            existingArtist.getAlbums().addAll(newAlbums);
        }
        if (artistRequest.getRoleIds() != null && !artistRequest.getRoleIds().isEmpty()) {
            log.debug("Adding roles with IDs: {}", artistRequest.getRoleIds());
            List<Role> newRoles = roleRepository.findAllById(artistRequest.getRoleIds());
            existingArtist.getRoles().addAll(newRoles);
        }
        if (artistRequest.getSongIds() != null && !artistRequest.getSongIds().isEmpty()) {
            log.debug("Adding songs with IDs: {}", artistRequest.getSongIds());
            List<Song> newSongs = songRepository.findAllById(artistRequest.getSongIds());
            existingArtist.getSongs().addAll(newSongs);
        }

        // 4. Guardar el artista
        log.info("Saving updated artist: {}", existingArtist.getName());
        Artist artistSave = artistRepository.save(existingArtist);

        // 5. Construir y devolver el ArtistResponse en el mismo método
        log.info("Artist with ID: {} updated successfully", artistSave.getId());
        return ArtistResponse.builder()
                .id(artistSave.getId())
                .name(artistSave.getName())
                .stageName(artistSave.getStageName())
                .type(artistSave.getType())
                .country(artistSave.getCountry())
                .debutDate(artistSave.getDebutDate())
                .build();
    }

    @Override
    public ArtistResponse findById(Integer id) {
        log.info("Finding artist with ID: {}", id); // Indicando que se está buscando un artist con un ID específico
        return artistRepository.findById(id)
                .map(artist -> {
                    log.debug("Found artist entity: {}", artist); // Album encontrado antes de mapearlo a AlbumResponse
                    return artistMapper.toArtistResponse(artist);
                })
                .orElseThrow(() -> {
                    log.error("Artist with ID: {} not found", id); // Error si el album no se encuentra
                    return new ArtistNotFoundException("Artist with ID " + id + " not found");
                });
    }


    @Override
    public Set<Artist> getArtistsByIds(Set<Integer> artistIds) {
        // Validación inicial para evitar problemas con IDs nulos o vacíos
        if (artistIds == null || artistIds.isEmpty()) {
            log.warn("No artist IDs provided.");
            return Collections.emptySet();
        }

        log.info("Fetching artists with IDs: {}", artistIds);

        // Recupera los artistas desde el repositorio
        Set<Artist> artists = new HashSet<>(artistRepository.findAllById(artistIds));

        // Verifica si no se encontraron artistas y lanza una excepción
        if (artists.isEmpty()) {
            log.error("No artists found for provided IDs: {}", artistIds);
            throw new ArtistNotFoundException("No artists found for provided IDs: " + artistIds);
        }

        log.info("Successfully fetched {} artists.", artists.size());
        return artists;
    }

    @Override
    public void deleteArtistById(Integer id) {
        log.info("Attempting to delete artist with ID: {}", id); // Log indicando el inicio del método

        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ArtistNotFoundException("Artist with ID " + id + " not found"));

        // Cambiar el estado del artist a false (inactivo)
        artist.setStatus(false);
        artistRepository.save(artist); // Guardar los cambios en la base de datos

        log.info("Successfully marked artist with ID: {} as inactive", id); // Confirmación de que el artist fue marcado como inactivo
    }
}
