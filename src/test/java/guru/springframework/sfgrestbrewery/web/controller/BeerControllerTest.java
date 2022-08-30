package guru.springframework.sfgrestbrewery.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.databind.ObjectMapper;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;

@WebFluxTest(BeerController.class)
class BeerControllerTest {

	@MockBean
	BeerService beerService;

	@Autowired
	WebTestClient webTestClient;

	@Autowired
	ObjectMapper objectMapper;

	BeerDto validBeer;

	@BeforeEach
	public void setUp() {
		validBeer = BeerDto.builder().id(UUID.randomUUID()).beerName("Beer1").beerStyle("PALE_ALE")
				.upc(BeerLoader.BEER_2_UPC).build();
	}

	@Test
	void getBeer() throws Exception {
		given(beerService.getById(any(UUID.class), any())).willReturn(validBeer);

		webTestClient.get().uri("/api/v1/beer/" + validBeer.getId().toString()).accept(MediaType.APPLICATION_JSON)
				.exchange().expectStatus().isOk().expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$.id").isEqualTo(validBeer.getId().toString()).jsonPath("$.beerName").isEqualTo("Beer1");
	}

	@Test
	void handlePost() throws Exception {
		// given
		BeerDto beerDto = validBeer;
		beerDto.setId(null);
		BeerDto savedDto = BeerDto.builder().id(UUID.randomUUID()).beerName("New Beer").build();
		String beerDtoJson = objectMapper.writeValueAsString(beerDto);

		given(beerService.saveNewBeer(any())).willReturn(savedDto);

		webTestClient.post().uri("/api/v1/beer/").contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(beerDtoJson)).exchange().expectStatus().isCreated();

	}

	@Test
	void handleUpdate() throws Exception {
		// given
		BeerDto beerDto = validBeer;
		beerDto.setId(null);
		String beerDtoJson = objectMapper.writeValueAsString(beerDto);

		// when

		webTestClient.put().uri("/api/v1/beer/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(beerDtoJson)).exchange().expectStatus().isNoContent();

		then(beerService).should().updateBeer(any(), any());

	}
}