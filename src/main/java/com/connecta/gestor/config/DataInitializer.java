package com.connecta.gestor.config;

import com.connecta.gestor.model.*;
import com.connecta.gestor.model.enums.RoleType;
import com.connecta.gestor.model.enums.TipoCampo;
import com.connecta.gestor.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private ServicoRepository servicoRepository;
    
    @Autowired
    private ServicoCampoRepository servicoCampoRepository;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("=================================================================");
        logger.info("Iniciando configuração do banco de dados...");
        logger.info("=================================================================");
        
        createRoles();
        createRootUser();
        
        logger.info("Verificando categorias base...");
        criarCategoriasBase();
        
        logger.info("Verificando serviços base...");
        criarServicosBase();
        
        logger.info("=================================================================");
        logger.info("Configuração do banco de dados concluída!");
        logger.info("=================================================================");
    }
    
    private void criarCategoriasBase() {
        List<Object[]> categoriasBase = Arrays.asList(
            new Object[]{"Meio Ambiente", "tree", "#4CAF50", 1},
            new Object[]{"Saneamento", "water_drop", "#2196F3", 2},
            new Object[]{"Infraestrutura", "construction", "#FF9800", 3},
            new Object[]{"Segurança", "security", "#F44336", 4},
            new Object[]{"Saúde", "local_hospital", "#E91E63", 5},
            new Object[]{"Documentação", "description", "#9C27B0", 6},
            new Object[]{"Educação", "school", "#3F51B5", 7},
            new Object[]{"Outros Serviços", "more_horiz", "#607D8B", 8}
        );
        
        for (Object[] cat : categoriasBase) {
            String nome = (String) cat[0];
            if (!categoriaRepository.existsByNome(nome)) {
                Categoria categoria = Categoria.builder()
                        .nome(nome)
                        .icone((String) cat[1])
                        .cor((String) cat[2])
                        .ordem((Integer) cat[3])
                        .ativo(true)
                        .build();
                categoriaRepository.save(categoria);
                logger.info("✅ Categoria criada: {}", nome);
            }
        }
    }
    
    private void criarServicosBase() {
        criarServicoSeNaoExistir("Poda de Árvore", "Meio Ambiente", 
            "Solicitação de poda de árvore em via pública", 15,
            Arrays.asList(
                new Object[]{TipoCampo.LOCALIZACAO, true, 1, "Informe o endereço exato da árvore"},
                new Object[]{TipoCampo.FOTO, true, 2, "Tire foto mostrando a árvore e o problema"},
                new Object[]{TipoCampo.DESCRICAO_DETALHADA, true, 3, "Descreva o motivo da solicitação"}
            ));
        
        criarServicoSeNaoExistir("Terreno com Mato Alto", "Meio Ambiente",
            "Denúncia de terreno com mato alto", 10,
            Arrays.asList(
                new Object[]{TipoCampo.LOCALIZACAO, true, 1, "Endereço completo do terreno"},
                new Object[]{TipoCampo.FOTO, true, 2, "Foto do terreno"},
                new Object[]{TipoCampo.NUMERO_IMOVEL, false, 3, null}
            ));
        
        criarServicoSeNaoExistir("Descarte Irregular de Lixo", "Meio Ambiente",
            "Denúncia de lixo descartado irregularmente", 7,
            Arrays.asList(
                new Object[]{TipoCampo.LOCALIZACAO, true, 1, "Local onde está o lixo"},
                new Object[]{TipoCampo.FOTO, true, 2, "Foto do lixo descartado"},
                new Object[]{TipoCampo.DESCRICAO_DETALHADA, false, 3, null}
            ));
        
        criarServicoSeNaoExistir("Vazamento de Água", "Saneamento",
            "Relato de vazamento de água em via pública", 3,
            Arrays.asList(
                new Object[]{TipoCampo.LOCALIZACAO, true, 1, "Local exato do vazamento"},
                new Object[]{TipoCampo.FOTO, true, 2, "Foto do vazamento"},
                new Object[]{TipoCampo.DESCRICAO_DETALHADA, true, 3, "Intensidade e características do vazamento"}
            ));
        
        criarServicoSeNaoExistir("Esgoto a Céu Aberto", "Saneamento",
            "Denúncia de esgoto a céu aberto", 5,
            Arrays.asList(
                new Object[]{TipoCampo.LOCALIZACAO, true, 1, "Local do problema"},
                new Object[]{TipoCampo.FOTO, true, 2, "Foto da situação"},
                new Object[]{TipoCampo.DESCRICAO_DETALHADA, false, 3, null}
            ));
        
        criarServicoSeNaoExistir("Falta de Água", "Saneamento",
            "Relato de falta de água", 2,
            Arrays.asList(
                new Object[]{TipoCampo.LOCALIZACAO, true, 1, "Endereço afetado"},
                new Object[]{TipoCampo.DATA_HORA, true, 2, "Desde quando está sem água?"},
                new Object[]{TipoCampo.DADOS_SOLICITANTE, true, 3, "Precisamos de contato para retorno"}
            ));
        
        criarServicoSeNaoExistir("Buraco na Rua", "Infraestrutura",
            "Solicitação de reparo de buraco em via pública", 15,
            Arrays.asList(
                new Object[]{TipoCampo.LOCALIZACAO, true, 1, "Endereço e ponto de referência"},
                new Object[]{TipoCampo.FOTO, true, 2, "Foto do buraco"},
                new Object[]{TipoCampo.METRAGEM, false, 3, "Tamanho aproximado do buraco"}
            ));
        
        criarServicoSeNaoExistir("Iluminação Pública", "Infraestrutura",
            "Reparo ou instalação de iluminação pública", 10,
            Arrays.asList(
                new Object[]{TipoCampo.LOCALIZACAO, true, 1, "Local do poste"},
                new Object[]{TipoCampo.FOTO, false, 2, "Foto do poste (se possível)"},
                new Object[]{TipoCampo.DESCRICAO_DETALHADA, true, 3, "Descreva o problema"}
            ));
        
        criarServicoSeNaoExistir("Denúncia de Crime", "Segurança",
            "Canal para denúncias anônimas", 1,
            Arrays.asList(
                new Object[]{TipoCampo.LOCALIZACAO, true, 1, "Local da ocorrência"},
                new Object[]{TipoCampo.DESCRICAO_DETALHADA, true, 2, "Descreva os fatos"},
                new Object[]{TipoCampo.DATA_HORA, false, 3, "Quando ocorreu?"}
            ));
        
        criarServicoSeNaoExistir("Foco de Dengue", "Saúde",
            "Denúncia de possível foco de dengue", 3,
            Arrays.asList(
                new Object[]{TipoCampo.LOCALIZACAO, true, 1, "Local do foco"},
                new Object[]{TipoCampo.FOTO, true, 2, "Foto do local"},
                new Object[]{TipoCampo.DESCRICAO_DETALHADA, true, 3, "Descreva a situação"}
            ));
        
        criarServicoSeNaoExistir("Alvará de Funcionamento", "Documentação",
            "Solicitação de alvará de funcionamento", 30,
            Arrays.asList(
                new Object[]{TipoCampo.DADOS_SOLICITANTE, true, 1, "Dados completos do solicitante"},
                new Object[]{TipoCampo.LOCALIZACAO, true, 2, "Endereço do estabelecimento"},
                new Object[]{TipoCampo.DOCUMENTOS_ANEXOS, true, 3, "Documentos necessários"},
                new Object[]{TipoCampo.METRAGEM, true, 4, "Área do estabelecimento"}
            ));
        
        criarServicoSeNaoExistir("Segunda Via de Documentos", "Documentação",
            "Solicitação de segunda via de documentos municipais", 7,
            Arrays.asList(
                new Object[]{TipoCampo.DADOS_SOLICITANTE, true, 1, "Dados completos"},
                new Object[]{TipoCampo.NUMERO_PROTOCOLO_ANTERIOR, false, 2, "Número do documento original (se tiver)"},
                new Object[]{TipoCampo.DESCRICAO_DETALHADA, true, 3, "Qual documento precisa?"}
            ));
        
        logger.info("✅ Serviços base criados/verificados");
    }
    
    private void criarServicoSeNaoExistir(String titulo, String categoriaNome, String descricao, 
            int prazo, List<Object[]> campos) {
        
        if (!servicoRepository.existsByTitulo(titulo)) {
            Categoria categoria = categoriaRepository.findByNome(categoriaNome)
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada: " + categoriaNome));
            
            Servico servico = Servico.builder()
                    .categoria(categoria)
                    .titulo(titulo)
                    .descricao(descricao)
                    .prazoAtendimentoDias(prazo)
                    .ativo(true)
                    .build();
            
            servico = servicoRepository.save(servico);
            
            for (Object[] campo : campos) {
                ServicoCampo servicoCampo = ServicoCampo.builder()
                        .servico(servico)
                        .campoTipo((TipoCampo) campo[0])
                        .obrigatorio((Boolean) campo[1])
                        .ordem((Integer) campo[2])
                        .instrucoes((String) campo[3])
                        .build();
                servicoCampoRepository.save(servicoCampo);
            }
            
            logger.info("✅ Serviço criado: {}", titulo);
        }
    }
    
    private void createRoles() {
        logger.info("Verificando roles do sistema...");
        
        for (RoleType roleType : RoleType.values()) {
            if (!roleRepository.findByNome(roleType).isPresent()) {
                Role role = new Role();
                role.setNome(roleType);
                role.setDescricao(roleType.getDescricao());
                role.setSistema(true);
                role.setAtivo(true);
                
                roleRepository.save(role);
                logger.info("✅ Role criada: {} - {}", roleType.name(), roleType.getDescricao());
            } else {
                logger.info("ℹ️  Role já existe: {} - {}", roleType.name(), roleType.getDescricao());
            }
        }
    }
    
    private void createRootUser() {
        String rootEmail = "lucaspenna@wimagroup.com.br";
        String rootPassword = "admin0946";
        
        logger.info("Verificando usuário root...");
        
        if (!userRepository.findByEmail(rootEmail).isPresent()) {
            Role superAdminRole = roleRepository.findByNome(RoleType.ROLE_SUPER_ADMIN)
                    .orElseThrow(() -> new RuntimeException(
                            "Role SUPER_ADMIN não encontrada. Execute a criação de roles primeiro."));
            
            User root = new User();
            root.setEmail(rootEmail);
            root.setSenha(passwordEncoder.encode(rootPassword));
            root.setNome("Administrador Root");
            root.setRole(superAdminRole);
            root.setAtivo(true);
            
            userRepository.save(root);
            
            logger.info("=================================================================");
            logger.info("✅ USUÁRIO ROOT CRIADO COM SUCESSO!");
            logger.info("=================================================================");
            logger.info("Email: {}", rootEmail);
            logger.info("Senha: {}", rootPassword);
            logger.info("Role: {}", superAdminRole.getNome().name());
            logger.info("=================================================================");
            logger.info("⚠️  IMPORTANTE: Por segurança, altere a senha após o primeiro login!");
            logger.info("=================================================================");
        } else {
            logger.info("ℹ️  Usuário root já existe: {}", rootEmail);
        }
    }
}
