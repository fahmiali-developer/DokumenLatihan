package com.supercode.service;

import com.supercode.entity.DetailPaymentAggregator;
import com.supercode.entity.DetailPaymentPos;
import com.supercode.entity.HeaderPayment;
import com.supercode.repository.DetailPaymentAggregatorRepository;
import com.supercode.repository.HeaderPaymentRepository;
import com.supercode.repository.PaymentMethodRepository;
import com.supercode.repository.PosRepository;
import com.supercode.util.MessageConstant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GeneralService {

    @Inject
    HeaderPaymentRepository headerPaymentRepository;

    @Inject
    PaymentMethodRepository paymentMethodRepository;

    @Inject
    PosRepository posRepository;

    @Inject
    DetailPaymentAggregatorRepository detailPaymentAggregatorRepository;


    public void saveHeaderPayment(MultipartFormDataInput file, String pmId, String branchId) {
        Map<String, List<InputPart>> formDataMap = file.getFormDataMap();
        List<InputPart> fileParts = formDataMap.get("file");

        HeaderPayment headerPayment = new HeaderPayment();
        headerPayment.setPmId(pmId);
        headerPayment.setBranchId(branchId);
        InputPart filePart = fileParts.get(0);
        headerPayment.setFileName(getFileName(filePart));
        headerPaymentRepository.persist(headerPayment);

    }
    private String getFileName(InputPart inputPart) {
        try {
            Map<String, List<String>> headers = inputPart.getHeaders();
            String contentDisposition = headers.get("Content-Disposition").get(0);

            for (String content : contentDisposition.split(";")) {
                if (content.trim().startsWith("filename")) {
                    return content.split("=")[1].trim().replaceAll("\"", "");
                }
            }
        } catch (Exception e) {
            return "unknown_file";
        }
        return "unknown_file";
    }

    public void saveDetailPayment(MultipartFormDataInput file, String pmId, String branchId) {
        String pmName="";
        try {
            pmName = paymentMethodRepository.getPaymentMethodByPmId(pmId);
            if(pmName.equalsIgnoreCase(MessageConstant.POS)){
                saveDetailPos(file, pmId, branchId);
            } else if (pmName.equalsIgnoreCase(MessageConstant.SHOPEEFOOD)){
                saveDetailShopeeFood(file, pmId, branchId);
            }else if(pmName.equalsIgnoreCase(MessageConstant.GRABFOOD)){
                saveDetailGrabFood(file, pmId, branchId);
            }
        }catch (Exception e){

        }
    }

    private void saveDetailGrabFood(MultipartFormDataInput file, String pmId, String branchId) {
        try {
            InputPart inputPart = getInputPart(file);
            try (InputStream inputStream = inputPart.getBody(InputStream.class, null);
                 Workbook workbook = new XSSFWorkbook(inputStream)) {

                Sheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    String transId = row.getCell(1).getStringCellValue();
                    Cell timeTransCell = row.getCell(2);
                    String formattedTimeDate = getDate(timeTransCell);

                    Cell timeCell = row.getCell(2);
                    String formattedTime = getTime(timeCell);


                    Cell grossAmountCell = row.getCell(3);
                    BigDecimal grossAmount = null;
                    if (grossAmountCell.getCellType() == CellType.NUMERIC) {
                        grossAmount = BigDecimal.valueOf(grossAmountCell.getNumericCellValue());
                    } else if (grossAmountCell.getCellType() == CellType.STRING) {
                        grossAmount = new BigDecimal(grossAmountCell.getStringCellValue());
                    }
                    Cell nettAmountCell = row.getCell(2);
                    BigDecimal nettAmount = null;
                    if (nettAmountCell.getCellType() == CellType.NUMERIC) {
                        nettAmount = BigDecimal.valueOf(nettAmountCell.getNumericCellValue());
                    } else if (nettAmountCell.getCellType() == CellType.STRING) {
                        nettAmount = new BigDecimal(nettAmountCell.getStringCellValue());
                    }

                    DetailPaymentAggregator dpa = new DetailPaymentAggregator();
                    dpa.setBranchId(branchId);
                    dpa.setPmId(pmId);
                    dpa.setTransDate(formattedTimeDate);
                    dpa.setTransId(transId);
                    dpa.setTransTime(formattedTime);
                    dpa.setGrossAmount(grossAmount);
                    dpa.setNetAmount(nettAmount);
                    dpa.setCharge(dpa.getGrossAmount().subtract(dpa.getNetAmount()));
                    dpa.setPaymentId(dpa.getTransId()+dpa.getPmId());
                    dpa.setSettlementDate(formattedTimeDate);
                    dpa.setSettlementTime(formattedTime);
                    detailPaymentAggregatorRepository.persist(dpa);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private InputPart getInputPart(MultipartFormDataInput file){
        Map<String, List<InputPart>> fileMap = file.getFormDataMap();

        List<InputPart> fileParts = fileMap.get("file"); // Sesuaikan key dengan yang dikirim di Postman
        if (fileParts == null || fileParts.isEmpty()) {
            throw new IllegalArgumentException("File Not Found!");
        }

        InputPart inputPart = fileParts.get(0);
        return inputPart;
    }


    private void saveDetailPos(MultipartFormDataInput file, String pmId, String branchId) {
        try {

            InputPart inputPart = getInputPart(file);
            try (InputStream inputStream = inputPart.getBody(InputStream.class, null);
                 Workbook workbook = new XSSFWorkbook(inputStream)) {

                Sheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;

                    String transId = row.getCell(5).getStringCellValue();
                    Cell grossAmountCell = row.getCell(3);
                    BigDecimal grossAmount = null;
                    if (grossAmountCell.getCellType() == CellType.NUMERIC) {
                        grossAmount = BigDecimal.valueOf(grossAmountCell.getNumericCellValue());
                    } else if (grossAmountCell.getCellType() == CellType.STRING) {
                        grossAmount = new BigDecimal(grossAmountCell.getStringCellValue());
                    }
                    Cell timeTransCell = row.getCell(1);
                    String formattedTimeDate = getDate(timeTransCell);

                    Cell timeCell = row.getCell(2);
                    String formattedTime = getTime(timeCell);
                    DetailPaymentPos detailPaymentPos = new DetailPaymentPos();
                    detailPaymentPos.setPmId(pmId);
                    detailPaymentPos.setBranchId(branchId);
                    detailPaymentPos.setTransDate(formattedTimeDate);
                    detailPaymentPos.setTransId(transId);
                    detailPaymentPos.setTransTime(formattedTime);
                    detailPaymentPos.setGrossAmount(grossAmount);
                    posRepository.persist(detailPaymentPos);
                }
            }
        }catch (Exception e){
                e.printStackTrace();
        }
    }

    private void saveDetailShopeeFood(MultipartFormDataInput file, String pmId, String branchId) {
        try {
            InputPart inputPart = getInputPart(file);
            try (InputStream inputStream = inputPart.getBody(InputStream.class, null);
                 Workbook workbook = new XSSFWorkbook(inputStream)) {

                Sheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    String transId = row.getCell(1).getStringCellValue();
                    Cell timeTransCell = row.getCell(2);
                    String formattedTimeDate = getDate(timeTransCell);

                    Cell timeCell = row.getCell(2);
                    String formattedTime = getTime(timeCell);


                    Cell grossAmountCell = row.getCell(3);
                    BigDecimal grossAmount = null;
                    if (grossAmountCell.getCellType() == CellType.NUMERIC) {
                        grossAmount = BigDecimal.valueOf(grossAmountCell.getNumericCellValue());
                    } else if (grossAmountCell.getCellType() == CellType.STRING) {
                        grossAmount = new BigDecimal(grossAmountCell.getStringCellValue());
                    }
                    Cell nettAmountCell = row.getCell(4);
                    BigDecimal nettAmount = null;
                    if (nettAmountCell.getCellType() == CellType.NUMERIC) {
                        nettAmount = BigDecimal.valueOf(nettAmountCell.getNumericCellValue());
                    } else if (nettAmountCell.getCellType() == CellType.STRING) {
                        nettAmount = new BigDecimal(nettAmountCell.getStringCellValue());
                    }

                    DetailPaymentAggregator dpa = new DetailPaymentAggregator();
                    dpa.setBranchId(branchId);
                    dpa.setPmId(pmId);
                    dpa.setTransDate(formattedTimeDate);
                    dpa.setTransId(transId);
                    dpa.setTransTime(formattedTime);
                    dpa.setGrossAmount(grossAmount);
                    dpa.setNetAmount(nettAmount);
                    dpa.setCharge(dpa.getGrossAmount().subtract(dpa.getNetAmount()));
                    dpa.setPaymentId(dpa.getTransId()+dpa.getPmId());
                    dpa.setSettlementTime(formattedTime);
                    detailPaymentAggregatorRepository.persist(dpa);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getDate(Cell timeTransCell){
        Date dateTime = timeTransCell.getDateCellValue();
        SimpleDateFormat timeFormatDate = new SimpleDateFormat("yyyy-MM-dd");
        String formattedTimeDate = timeFormatDate.format(dateTime);
        return formattedTimeDate;
    }

    private String getTime(Cell timeTransCell){
        Date date = timeTransCell.getDateCellValue();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String formattedTime = timeFormat.format(date);
        return formattedTime;
    }
}
