package net.Verinario.VeterinarioServer.service;


import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import net.Verinario.VeterinarioServer.exception.ResourceNotFoundException;
import net.Verinario.VeterinarioServer.exception.ResourceNotModifiedException;
import net.Verinario.VeterinarioServer.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.Verinario.VeterinarioServer.repository.TipoAnimalRepository;
import net.Verinario.VeterinarioServer.entity.AnimalEntity;
import net.Verinario.VeterinarioServer.exception.CannotPerformOperationException;
import net.Verinario.VeterinarioServer.helper.RandomHelper;
import net.Verinario.VeterinarioServer.helper.ValidationHelper;
import net.Verinario.VeterinarioServer.repository.AnimalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import net.Verinario.VeterinarioServer.repository.TipoAnimalRepository;

@Service
public class AnimalService {

    @Autowired
    TipoAnimalService oTipoAnimalService;
    @Autowired
    TipoAnimalRepository oTipoAnimalRepository;

    @Autowired
    AnimalRepository oAnimalRepository;

    @Autowired
    AuthService oAuthService;


    private final String[] NAMES = {"", "Mico", "Chispa", "Rayo", "Pluton", "Chico", "Luna", "Lola", "Coco",
        "Linda", "Noa", "Nina", "Toby", "Rocky", "Thor", "Simba", "Bruno", "Nico", "Bimba", "Max"};

    private final String[] COLORS = {"Negro", "Marrón", "Verde", "Gris", "Rosa", "Purpura", "Blanco",
        "Rojo", "Amarillo"};

    private final String[] RAZAS = {"RogWailer", "Pitbull", "Pug", "Pug", "BUlldog Frances", "Egipcio", "Orejon",
    "Peludi", "Enano"};

    

    public void validate(Long id) {
        if (!oAnimalRepository.existsById(id)) {
            throw new ResourceNotFoundException("id " + id + " not exist");
        }
    }

    public void validate(AnimalEntity oAnimalEntity) {
        ValidationHelper.validateStringLength(oAnimalEntity.getNombre_animal(), 2, 50, "campo Name de Animal (el campo debe tener longitud de 2 a 50 caracteres)");
        ValidationHelper.validateStringLength(oAnimalEntity.getColor(), 2, 50, "campo primer Surname de Animal (el campo debe tener longitud de 2 a 50 caracteres)");
        ValidationHelper.validateStringLength(oAnimalEntity.getRaza(), 2, 50, "campo segundo Surname de Animal (el campo debe tener longitud de 2 a 50 caracteres)");
        ValidationHelper.validateDate(oAnimalEntity.getFecha_nac(), null, null, " campo fecha de animal");
        ValidationHelper.validateRPP(oAnimalEntity.getVacunado());
    }
 
    public AnimalEntity get(Long id) {
       oAuthService.OnlyAdminsOrOwnUsersData(id);
        try {
            return oAnimalRepository.findById(id).get();
        } catch (Exception ex) {
            throw new ResourceNotFoundException("id " + id + " not exist");
        }
    }

    public Long count() {
        oAuthService.OnlyAdmins();
        return oAnimalRepository.count();
    }


    /* 
    public Page<AnimalEntity> getPage(Long id_TipoAnimal, int page, int size , String strFilter) {
        //oAuthService.OnlyAdmins();
        Pageable oPageable = PageRequest.of(page, size);
        if (id_TipoAnimal == null && strFilter == null) {
            return oAnimalRepository.findAll(oPageable);
        }else// else if (strFilter == null) {
            return oAnimalRepository.findByTipoAnimalId(id_TipoAnimal,  oPageable);
        } //else con solo filtro
    //}
    */

    public Page<AnimalEntity> getPage(Pageable oPageable, String strFilter, Long id_TipoAnimal) {
        oAuthService.OnlyAdmins();
        Page<AnimalEntity> oPage = null;
        if (id_TipoAnimal != null) {
            if (strFilter == null || strFilter.isEmpty() || strFilter.trim().isEmpty()) {
                oPage = oAnimalRepository.findByTipoAnimalId(id_TipoAnimal, oPageable);
            } else {
                oPage = oAnimalRepository
                        .findByTipoAnimalIdAndNombre_animalIgnoreCaseContainingOrColorIgnoreCaseContainingOrRazaIgnoreCaseContaining( id_TipoAnimal, strFilter, strFilter, strFilter, oPageable);
            }
        } else {
            if (strFilter == null || strFilter.isEmpty() || strFilter.trim().isEmpty()) {
                oPage = oAnimalRepository.findAll(oPageable);
            } else {
                oPage = oAnimalRepository
                        .findByNombre_animalIgnoreCaseContainingOrColorIgnoreCaseContainingOrRazaIgnoreCaseContaining(strFilter, strFilter, strFilter, oPageable);
            }
        }
        return oPage;
    }
    
    public Long create(AnimalEntity oNewAnimalEntity) {
       oAuthService.OnlyAdmins();
       validate(oNewAnimalEntity);
        oNewAnimalEntity.setId(0L);
         //oNewAnimalEntity.setToken(RandomHelper.getToken(100));
        return oAnimalRepository.save(oNewAnimalEntity).getId();

    }

    public Long update(AnimalEntity oAnimalEntity) {
        oAuthService.OnlyAdmins();
        validate(oAnimalEntity.getId());
        oAnimalRepository.save(oAnimalEntity);
        return oAnimalEntity.getId();
    }
    
  

    public Long delete(Long id) {
      oAuthService.OnlyAdmins();
        if (oAnimalRepository.existsById(id)) {
            oAnimalRepository.deleteById(id);
            if (oAnimalRepository.existsById(id)) {
                throw new ResourceNotModifiedException("can't remove register " + id);
            } else {
                return id;
            }
        } else {
            throw new ResourceNotModifiedException("id " + id + " not exist");
        }
    }
  
    public AnimalEntity generate() {
      oAuthService.OnlyAdmins();
        return oAnimalRepository.save(generateRandomAnimal());
    }

    public Long generateSome(Integer amount) {
      oAuthService.OnlyAdmins();
        List<AnimalEntity> AnimalList = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            AnimalEntity oAnimalEntity = generateRandomAnimal();
            oAnimalRepository.save(oAnimalEntity);
            AnimalList.add(oAnimalEntity);
        }
        return oAnimalRepository.count();
    }
    

    public AnimalEntity getOneRandom() {
        if (count() > 0) {
            AnimalEntity oAnimalEntity = null;
            int iPosicion = RandomHelper.getRandomInt(0, (int) oAnimalRepository.count() - 1);
            Pageable oPageable = PageRequest.of(iPosicion, 1);
            Page<AnimalEntity> AnimalPage = oAnimalRepository.findAll(oPageable);
            List<AnimalEntity> AnimalList = AnimalPage.getContent();
            oAnimalEntity = oAnimalRepository.getById(AnimalList.get(0).getId());
            return oAnimalEntity;
        } else {
            throw new CannotPerformOperationException("no hay Animals en la base de datos");
        }
    }
    
    private AnimalEntity generateRandomAnimal() {
        AnimalEntity oAnimalEntity = new AnimalEntity();
        oAnimalEntity.setNombre_animal(generateNombre_animal());
        oAnimalEntity.setColor(generateColor());
        oAnimalEntity.setRaza(generateRaza());
        oAnimalEntity.setFecha_nac(RandomHelper.getRadomDateTime());
        oAnimalEntity.setVacunado(RandomHelper.getRandomInt2(0, 1));
        oAnimalEntity.setPeso(RandomHelper.getRadomDouble(5, 25));
        int totalTipoAnimals = (int) oTipoAnimalRepository.count();
        int randomTipoAnimalId = RandomHelper.getRandomInt(1, totalTipoAnimals);
        oTipoAnimalRepository.findById((long) randomTipoAnimalId)
                .ifPresent(oAnimalEntity.setTipoanimal(randomTipoAnimalId););


        return oAnimalEntity;
    }
    

    private String generateNombre_animal() {
        return NAMES[RandomHelper.getRandomInt(0, NAMES.length - 1)].toLowerCase();
    }

    private String generateColor() {
        return COLORS[RandomHelper.getRandomInt(0, COLORS.length - 1)].toLowerCase();
    }

    private String generateRaza() {
        return RAZAS[RandomHelper.getRandomInt(0, RAZAS.length - 1)].toLowerCase();
    }

    private String getFromList(List<String> list) {
        int randomNumber = RandomHelper.getRandomInt(0, list.size() - 1);
        String value = list.get(randomNumber);
        list.remove(randomNumber);
        return value;
    }
   
}
