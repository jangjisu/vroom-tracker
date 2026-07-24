package com.restroute.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restroute.domain.RestFoodImageEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class RestFoodImageRepositoryTest {

    @Autowired
    private RestFoodImageRepository restFoodImageRepository;

    @Test
    @DisplayName("메뉴 이미지를 저장하고 id로 다시 조회한다")
    void save_thenFindByIdReturnsStoredImage() {
        byte[] detail = {1, 2, 3};
        byte[] list = {4, 5, 6};
        restFoodImageRepository.save(RestFoodImageEntity.of(1L, detail, list));

        assertThat(restFoodImageRepository.findById(1L)).isPresent();
        assertThat(restFoodImageRepository.existsById(1L)).isTrue();
    }

    @Test
    @DisplayName("id로 메뉴 이미지를 삭제할 수 있다")
    void deleteById_removesStoredImage() {
        restFoodImageRepository.save(RestFoodImageEntity.of(1L, new byte[] {1}, new byte[] {2}));

        restFoodImageRepository.deleteById(1L);

        assertThat(restFoodImageRepository.existsById(1L)).isFalse();
    }
}
