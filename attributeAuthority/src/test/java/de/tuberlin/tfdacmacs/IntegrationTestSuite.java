package de.tuberlin.tfdacmacs;

import de.tuberlin.tfdacmacs.attributeauthority.attribute.AttributeController;
import de.tuberlin.tfdacmacs.attributeauthority.attribute.db.AttributeDB;
import de.tuberlin.tfdacmacs.attributeauthority.init.gpp.client.GPPFeignClient;
import de.tuberlin.tfdacmacs.attributeauthority.user.db.UserDB;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.AttributeKeyGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.basics.factory.AttributeTestFactory;
import de.tuberlin.tfdacmacs.basics.factory.GPPTestFactory;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GlobalPublicParameterDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AttributeAuthorityApplication.class,
        webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestSuite {

    @Autowired
    protected TestRestTemplate restTemplate;

    // Mock beans
    @MockBean
    protected GPPFeignClient gppFeignClient;

    // Controller
    @Autowired
    protected AttributeController attributeController;

    // Utils and Services
    @Autowired
    protected PairingGenerator pairingGenerator;
    @Autowired
    protected AttributeKeyGenerator attributeKeyGenerator;

    // DBs
    @Autowired
    protected AttributeDB attributeDB;
    @Autowired
    protected UserDB userDB;

    // Factories
    @Autowired
    protected GPPTestFactory gppTestFactory;
    @Autowired
    protected AttributeTestFactory attributeTestFactory;

    @Before
    public void mockGPPRequest() {
        GlobalPublicParameterDTO globalPublicParameterDTO = gppTestFactory.createDTO();
        doReturn(globalPublicParameterDTO).when(gppFeignClient).getGPP();
    }

    @After
    public void cleanUp() {
        attributeDB.drop();
        userDB.drop();
    }
}