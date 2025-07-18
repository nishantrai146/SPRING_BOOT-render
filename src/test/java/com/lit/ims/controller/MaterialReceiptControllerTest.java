package com.lit.ims.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.lit.ims.dto.MaterialReceiptDTO;
import com.lit.ims.service.MaterialReceiptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MaterialReceiptController.class)
public class MaterialReceiptControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private MaterialReceiptService materialReceiptService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSaveReceipt()  throws Exception{
        MaterialReceiptDTO dto=new MaterialReceiptDTO();
        dto.set
    }


}
