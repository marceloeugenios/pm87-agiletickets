package br.com.caelum.agiletickets.controllers;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import br.com.caelum.agiletickets.domain.Agenda;
import br.com.caelum.agiletickets.domain.DiretorioDeEstabelecimentos;
import br.com.caelum.agiletickets.models.Espetaculo;
import br.com.caelum.agiletickets.models.Periodicidade;
import br.com.caelum.agiletickets.models.Sessao;
import br.com.caelum.agiletickets.models.TipoDeEspetaculo;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.util.test.MockResult;
import br.com.caelum.vraptor.util.test.MockValidator;
import br.com.caelum.vraptor.validator.ValidationException;

public class EspetaculosControllerTest {

	private @Mock Agenda agenda;
	private @Mock DiretorioDeEstabelecimentos estabelecimentos;
	private @Spy Validator validator = new MockValidator();
	private @Spy Result result = new MockResult();
	private Espetaculo espetaculo;
	private LocalDate inicio;
	private LocalDate fim;
	private LocalTime horario;
	private int quantidadeSessoes;
	
	private EspetaculosController controller;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		controller = new EspetaculosController(result, validator, agenda, estabelecimentos);
		espetaculo = new Espetaculo();
		inicio = new LocalDate(2014, 8, 21);
		fim = new LocalDate(2014, 8, 21);
		horario = new LocalTime(20, 0, 0); 
	}

	@Test(expected=ValidationException.class)
	public void naoDeveCadastrarEspetaculosSemNome() throws Exception {
		Espetaculo espetaculo = new Espetaculo();
		espetaculo.setDescricao("uma descricao");

		controller.adiciona(espetaculo);

		verifyZeroInteractions(agenda);
	}

	@Test(expected=ValidationException.class)
	public void naoDeveCadastrarEspetaculosSemDescricao() throws Exception {
		Espetaculo espetaculo = new Espetaculo();
		espetaculo.setNome("um nome");

		controller.adiciona(espetaculo);

		verifyZeroInteractions(agenda);
	}

	@Test
	public void deveCadastrarEspetaculosComNomeEDescricao() throws Exception {
		Espetaculo espetaculo = new Espetaculo();
		espetaculo.setNome("um nome");
		espetaculo.setDescricao("uma descricao");

		controller.adiciona(espetaculo);

		verify(agenda).cadastra(espetaculo);
	}
	
	@Test
	public void deveRetornarNotFoundSeASessaoNaoExiste() throws Exception {
		when(agenda.sessao(1234l)).thenReturn(null);

		controller.sessao(1234l);

		verify(result).notFound();
	}

	@Test(expected=ValidationException.class)
	public void naoDeveReservarZeroIngressos() throws Exception {
		when(agenda.sessao(1234l)).thenReturn(new Sessao());

		controller.reserva(1234l, 0);

		verifyZeroInteractions(result);
	}

	@Test(expected=ValidationException.class)
	public void naoDeveReservarMaisIngressosQueASessaoPermite() throws Exception {
		Sessao sessao = new Sessao();
		sessao.setTotalIngressos(3);

		when(agenda.sessao(1234l)).thenReturn(sessao);

		controller.reserva(1234l, 5);

		verifyZeroInteractions(result);
	}

	@Test
	public void deveReservarSeASessaoTemIngressosSuficientes() throws Exception {
		Espetaculo espetaculo = new Espetaculo();
		espetaculo.setTipo(TipoDeEspetaculo.TEATRO);

		Sessao sessao = new Sessao();
		sessao.setPreco(new BigDecimal("10.00"));
		sessao.setTotalIngressos(5);
		sessao.setEspetaculo(espetaculo);

		when(agenda.sessao(1234l)).thenReturn(sessao);

		controller.reserva(1234l, 3);

		assertThat(sessao.getIngressosDisponiveis(), is(2));
	}

	@Test
	public void criaApenasUmaSessaoDiaria() throws Exception {
		quantidadeSessoes = 1;
		Periodicidade periodicidade = Periodicidade.DIARIA;
		List<Sessao> sessoes = espetaculo.criaSessoes(inicio, inicio, horario, periodicidade);
		Sessao sessao = sessoes.get(0);
		
		org.junit.Assert.assertEquals(inicio.toDateTime(horario), sessao.getInicio());
		org.junit.Assert.assertEquals(quantidadeSessoes, sessoes.size());
	}
	
	@Test
	public void criaDuasSessoesDiaria() throws Exception {
		quantidadeSessoes = 2;
		Periodicidade periodicidade = Periodicidade.DIARIA;
		List<Sessao> sessoes = espetaculo.criaSessoes(inicio, inicio.plusDays(quantidadeSessoes), horario, periodicidade);
		Sessao primeiraSessao = sessoes.get(0);
		Sessao segundaSessao = sessoes.get(1);
		
		org.junit.Assert.assertEquals(inicio.toDateTime(horario), primeiraSessao.getInicio());
		org.junit.Assert.assertEquals(inicio.plusDays(1).toDateTime(horario), segundaSessao.getInicio());
		
		org.junit.Assert.assertEquals(quantidadeSessoes, sessoes.size());
		
	}
	
	@Test
	public void criaDezSessoesDiaria() throws Exception {
		quantidadeSessoes = 10;
		Periodicidade periodicidade = Periodicidade.DIARIA;
		List<Sessao> sessoes = espetaculo.criaSessoes(inicio, inicio.plusDays(quantidadeSessoes), horario, periodicidade);
		Sessao primeiraSessao = sessoes.get(0);
		Sessao ultimaSessao = sessoes.get(9);
		
		org.junit.Assert.assertEquals(inicio.toDateTime(horario), primeiraSessao.getInicio());
		org.junit.Assert.assertEquals(inicio.plusDays(9).toDateTime(horario), ultimaSessao.getInicio());		
		org.junit.Assert.assertEquals(quantidadeSessoes, sessoes.size());
		
	}
	
	@Test
	public void criaApenasUmaSessaoSemanal() throws Exception {
		quantidadeSessoes = 1;
		Periodicidade periodicidade = Periodicidade.SEMANAL;
		List<Sessao> sessoes = espetaculo.criaSessoes(inicio, inicio, horario, periodicidade);
		Sessao sessao = sessoes.get(0);
		
		org.junit.Assert.assertEquals(inicio.toDateTime(horario), sessao.getInicio());
		org.junit.Assert.assertEquals(quantidadeSessoes, sessoes.size());
	}
	
	@Test
	public void criaDuasSessoesSemanal() throws Exception {
		quantidadeSessoes = 2;
		Periodicidade periodicidade = Periodicidade.SEMANAL;
		fim = inicio.plusDays(periodicidade.getDias() * quantidadeSessoes);
		List<Sessao> sessoes = espetaculo.criaSessoes(inicio, fim, horario, periodicidade);
		Sessao primeiraSessao = sessoes.get(0);
		Sessao segundaSessao = sessoes.get(1);
		
		org.junit.Assert.assertEquals(inicio.toDateTime(horario), primeiraSessao.getInicio());
		org.junit.Assert.assertEquals(inicio.plusDays(7).toDateTime(horario), segundaSessao.getInicio());
		org.junit.Assert.assertEquals(quantidadeSessoes, sessoes.size());
		
	}
	
	@Test
	public void criaDezSessoesSemanal() throws Exception {
		quantidadeSessoes = 10;
		Periodicidade periodicidade = Periodicidade.SEMANAL;
		
		fim = inicio.plusDays(periodicidade.getDias() * quantidadeSessoes);
		List<Sessao> sessoes = espetaculo.criaSessoes(inicio, fim, horario, periodicidade);
		Sessao primeiraSessao = sessoes.get(0);
		Sessao ultimaSessao = sessoes.get(9);
		
		org.junit.Assert.assertEquals(inicio.toDateTime(horario), primeiraSessao.getInicio());
		org.junit.Assert.assertEquals(inicio.plusDays(63).toDateTime(horario), ultimaSessao.getInicio());
		org.junit.Assert.assertEquals(quantidadeSessoes, sessoes.size());
	}
}
