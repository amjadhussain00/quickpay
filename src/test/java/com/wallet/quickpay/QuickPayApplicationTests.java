package com.wallet.quickpay;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.quickpay.model.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class QuickPayApplicationTests {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void contextLoads() {
		assertThat(mockMvc).isNotNull();
	}

	@Test
	void shouldReturnAllWallets() throws Exception {
		mockMvc.perform(get("/api/wallets")).andDo(print())
				.andExpect(status().isOk());
	}
	@Test
	void shouldReturnErrorForWalletNotFoundInCredit() throws Exception {
		String id = invalidWalletId();
		mockMvc.perform(post("/api/credit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(prepareRequest(id,new BigDecimal("10.0"))))
				.andDo(print()).andExpect(status().isNotFound())
				.andExpect(content().string(containsString(String.format("Wallet %s does not exist", id))));
	}
	@Test
	void shouldReturnErrorForMinimumAmountInCredit() throws Exception {
		String id = selectRandomWallet().getId();
		mockMvc.perform(post("/api/credit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(prepareRequest(id, new BigDecimal("9.0"))))
				.andDo(print()).andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("Amount should be minimum 10")));
	}
	@Test
	void shouldReturnErrorForMaximumAmountInCredit() throws Exception {
		String id = selectRandomWallet().getId();
		mockMvc.perform(post("/api/credit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(prepareRequest(id, new BigDecimal("10000.01"))))
				.andDo(print()).andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("Amount can be maximum 10000")));
	}
	@Test
	void shouldReturnSuccessfulCredit() throws Exception {
		String id = selectRandomWallet().getId();
		String amount = "11";
		mockMvc.perform(post("/api/credit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(prepareRequest(id, new BigDecimal(amount))))
				.andDo(print()).andExpect(status().isCreated())
				.andExpect(jsonPath("$.walletId").value(id))
				.andExpect(jsonPath("$.amount").value(amount));
	}
	@Test
	void shouldReturnErrorForWalletNotFoundInDebit() throws Exception {
		String id = invalidWalletId();
		mockMvc.perform(post("/api/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(prepareRequest(id, new BigDecimal("10.0"))))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(content().string(containsString(String.format("Wallet %s does not exist",id))));
	}
	@Test
	void shouldReturnErrorForMinimumAmountInDebit() throws Exception {
		String id = selectRandomWallet().getId();
		mockMvc.perform(post("/api/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(prepareRequest(id, new BigDecimal("0"))))
				.andDo(print()).andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("Amount should be minimum 0.01")));
	}
	@Test
	void shouldReturnErrorForMaximumAmountInDebit() throws Exception {
		String id = selectRandomWallet().getId();
		mockMvc.perform(post("/api/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(prepareRequest(id, new BigDecimal("5000.01"))))
				.andDo(print()).andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("Amount can be maximum 5000")));
	}
	@Test
	void shouldReturnErrorForInsufficientFundInDebit() throws Exception {
		Wallet wallet = selectRandomWallet();
		mockMvc.perform(post("/api/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(prepareRequest(wallet.getId(),wallet.getBalance().add(BigDecimal.ONE))))
				.andDo(print()).andExpect(status().isNotAcceptable())
				.andExpect(content().string(containsString(String.format("Wallet %s does not have sufficient fund", wallet.getId()))));
	}
	@Test
	void shouldReturnSuccessfulDebit() throws Exception {
		String id = selectRandomWallet().getId();
		String amount = "1";
		mockMvc.perform(post("/api/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(prepareRequest(id,new BigDecimal(amount))))
				.andDo(print()).andExpect(status().isCreated())
				.andExpect(jsonPath("$.walletId").value(id))
				.andExpect(jsonPath("$.amount").value(amount));
	}
	@Test
	void shouldReturnErrorForConcurrentRequestInDebitForSameWallet() throws Exception {
		String id = selectRandomWallet().getId();
		ExecutorService executorService = null;
		try {
			BigDecimal amount = new BigDecimal("13");
			executorService = Executors.newFixedThreadPool(3);
			Collection<Callable<MvcResult>> callables = new ArrayList<>();
			IntStream.rangeClosed(1, 3).forEach(i-> {
				callables.add(() -> mockMvc.perform(post("/api/debit")
								.contentType(MediaType.APPLICATION_JSON).content(prepareRequest(id, amount)))
								.andReturn());
			});
			List<Future<MvcResult>> futures = executorService.invokeAll(callables);
			List<Integer> statuses = new ArrayList<>();
			for(Future<MvcResult> future : futures){
				statuses.add(future.get().getResponse().getStatus());
			}
			assertTrue(statuses.contains(HttpStatus.CONFLICT.value()));
		}finally {
			executorService.shutdown();
		}
	}
	@Test
	void shouldDebitSuccessForConcurrentRequestOnDifferentWallet() throws Exception {
		ExecutorService executorService = null;
		List<String> walletIds = allWallets().stream().map(Wallet::getId).collect(Collectors.toList());
		try {
			BigDecimal amount = new BigDecimal("13");
			executorService = Executors.newFixedThreadPool(3);
			Collection<Callable<MvcResult>> callables = new ArrayList<>();
			IntStream.rangeClosed(1, 3).forEach(i-> {
				callables.add(() -> mockMvc.perform(post("/api/debit")
								.contentType(MediaType.APPLICATION_JSON).content(prepareRequest(walletIds.get(i), amount)))
						.andReturn());
			});
			List<Future<MvcResult>> futures = executorService.invokeAll(callables);
			List<Integer> statuses = new ArrayList<>();
			for(Future<MvcResult> future : futures){
				statuses.add(future.get().getResponse().getStatus());
			}
			assertFalse(statuses.contains(HttpStatus.CONFLICT.value()));
		}finally {
			executorService.shutdown();
		}
	}

	@Test
	void shouldReturnPaginatedAllTransactions() throws Exception {
		mockMvc.perform(get("/api/transactions")).andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalItems").isNumber())
				.andExpect(jsonPath("$.totalPages").isNumber())
				.andExpect(jsonPath("$.currentPage").isNumber())
				.andExpect(jsonPath("$.transactions").isArray());
	}
	@Test
	void shouldReturnPaginatedTransactionsForAWallet() throws Exception {
		String id = selectRandomWallet().getId();
		mockMvc.perform(get("/api/transactions").param("walletId",id)).andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalItems").isNumber())
				.andExpect(jsonPath("$.totalPages").isNumber())
				.andExpect(jsonPath("$.currentPage").isNumber())
				.andExpect(jsonPath("$.transactions").isArray());
	}
	private String invalidWalletId() {
		return UUID.randomUUID().toString();
	}
	private Wallet selectRandomWallet() throws Exception {
		return allWallets().stream().findAny().get();
	}
	private List<Wallet> allWallets() throws Exception {
		return objectMapper.readValue(mockMvc.perform(get("/api/wallets")).andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString(),new TypeReference<List<Wallet>>(){});
	}
	private String prepareRequest(String walletId, BigDecimal amount){
		return "{\"walletId\":\""+walletId+"\", \"amount\":"+amount+", \"currency\":\"EURO\"}";
	}

}
