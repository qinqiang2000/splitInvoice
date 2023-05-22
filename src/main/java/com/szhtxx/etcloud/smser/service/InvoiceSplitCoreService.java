package com.szhtxx.etcloud.smser.service;

import com.szhtxx.etcloud.smser.methods.smser.*;
import org.slf4j.*;
import java.math.*;
import com.szhtxx.etcloud.smser.enums.*;
import java.io.*;
import java.lang.reflect.*;
import org.apache.commons.beanutils.*;
import com.szhtxx.etcloud.smser.constant.*;
import com.szhtxx.etcloud.smser.dto.*;
import java.util.*;

public class InvoiceSplitCoreService
{
    private static Logger LOG;
    private static BackCalcUtilMethods calcUtilMethods;
    
    static {
        InvoiceSplitCoreService.LOG = LoggerFactory.getLogger(InvoiceSplitCoreService.class);
        InvoiceSplitCoreService.calcUtilMethods = new BackCalcUtilMethods();
    }
    
    public static void dealDetailByRule(final BillDetailDto curDto, final SmruleConfigDto configDto, final List<BillDetailDto> billDetailDtos, final int curIndex) {
        final Map<String, Integer> decimalMap = getDigitByRuleConfig(configDto);
        final int amtNumber = decimalMap.get("amtNumber");
        final int priceNumber = decimalMap.get("priceNumber");
        BigDecimal amounts = curDto.getAmounts();
        BigDecimal amountsInc = curDto.getAmountsIncTax();
        final BigDecimal taxAmt = curDto.getTaxAmt();
        BigDecimal amts = curDto.getAmts();
        BigDecimal price = curDto.getPrice();
        BigDecimal priceInc = curDto.getPriceIncTax();
        final int includeTax = curDto.getIncludeTax();
        final BigDecimal tmpAmountsInc = curDto.getAmountsIncTax();
        final BigDecimal lineAmountErr = configDto.getLineAmountErr();
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            amts = amts.setScale(amtNumber, 4);
            amountsInc = amountsInc.setScale(2, 4);
            amounts = amounts.setScale(2, 4);
            if (EnumType.YOrNEnum.YES.getValue() == includeTax) {
                priceInc = priceInc.setScale(priceNumber, 4);
                if (amts.multiply(priceInc).subtract(amountsInc).abs().compareTo(lineAmountErr) > 0) {
                    priceInc = InvoiceSplitCoreService.calcUtilMethods.recursionPrice(amountsInc, amts, priceNumber, configDto);
                }
                if (amts.multiply(price).subtract(amounts).abs().compareTo(lineAmountErr) > 0) {
                    price = InvoiceSplitCoreService.calcUtilMethods.recursionPrice(amounts, amts, priceNumber, configDto);
                }
            }
            else {
                price = price.setScale(priceNumber, 4);
                if (amts.multiply(price).subtract(amounts).abs().compareTo(lineAmountErr) > 0) {
                    price = InvoiceSplitCoreService.calcUtilMethods.recursionPrice(amounts, amts, 0, configDto);
                }
                if (amts.multiply(priceInc).subtract(amountsInc).abs().compareTo(lineAmountErr) > 0) {
                    priceInc = InvoiceSplitCoreService.calcUtilMethods.recursionPrice(amountsInc, amts, 0, configDto);
                }
            }
            curDto.setAmts(amts);
            curDto.setPrice(price);
            curDto.setPriceIncTax(priceInc);
        }
        else {
            amountsInc = amountsInc.setScale(2, 4);
            amounts = tmpAmountsInc.subtract(taxAmt).setScale(2, 4);
        }
        curDto.setTaxAmt(taxAmt);
        curDto.setAmounts(amounts);
        curDto.setAmountsIncTax(amountsInc);
        final int j = curIndex + 1;
        if (j < billDetailDtos.size()) {
            final BillDetailDto disDto = billDetailDtos.get(j);
            if (InvoiceCoreService.isDisLine(disDto)) {
                BigDecimal disAmt = disDto.getAmounts();
                BigDecimal disAmtInc = disDto.getAmountsIncTax();
                final BigDecimal oDisAmtInc = disDto.getAmountsIncTax();
                BigDecimal disTaxAmt = disDto.getTaxAmt();
                disAmtInc = disAmtInc.setScale(2, 4);
                disAmt = disAmt.setScale(2, 4);
                disTaxAmt = oDisAmtInc.subtract(disAmt).setScale(2, 4);
                disDto.setAmountsIncTax(disAmtInc);
                disDto.setAmounts(disAmt);
                disDto.setTaxAmt(disTaxAmt);
                billDetailDtos.set(j, disDto);
            }
        }
    }
    
    /* 
     * 该方法首先备份了单据明细detailDtos，然后对每个商品行应用拆分规则。
     * 如果拆分规则指定了拆分数量，则将商品行拆分为指定数量的行。
     * 否则，将商品行拆分为数量相等的行。如果拆分后的行的数量小于指定的数量，则将剩余的金额添加到最后一行。
     * 拆分后的行将添加到新的列表中，并返回该列表。
     * 
     * 在拆分过程中，该方法还会计算每个商品行的金额和税额，并根据拆分规则进行调整。
     * 如果拆分后的行的金额或税额与原始行的金额或税额相差超过了拆分规则中指定的误差范围，则会进行递归调整，直到满足误差范围为止。
     * 该方法还会根据拆分规则对商品行进行排序，并将折扣行与商品行进行匹配，以便在后续的拆分过程中进行处理。
     * 最后，该方法会将拆分后的商品行列表返回。
     * 
     * 递归函数
     * aggrAmt：已累计拆分金额
     */
    public static List<BillDetailDto> goodsLineSplit(List<BillDetailDto> detailDtos, SmruleConfigDto configDto,
                                                     BigDecimal aggrAmt, boolean hasDisLine, BigDecimal allAmount,
                                                     List<BigDecimal> splitAmounts) throws IllegalAccessException, InvocationTargetException {
        int splitGoodsType = configDto.getSplitGoodsWithNumber();
        BigDecimal invLimitAmt = configDto.getFinalLimitAmt();
        BigDecimal lineTaxAmtErr = configDto.getLineTaxAmtErr();
        BigDecimal lineAmtErr = configDto.getLineAmountErr();
        List<BillDetailDto> originDetailDtos = new ArrayList<>();

        // 备份detailDtos列表
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(detailDtos);
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            originDetailDtos = (List<BillDetailDto>)ois.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            InvoiceSplitCoreService.LOG.error("商品拆分时，备份数据异常 e={}", e);
        }

        BigDecimal originAggrAmt = aggrAmt;
        
        int index = 0;
        int splitAmountIndex = 0;
        if (splitAmounts != null && !splitAmounts.isEmpty()) {
            invLimitAmt = splitAmounts.get(splitAmountIndex);
        }
        
        allAmount = allAmount.subtract(aggrAmt);
        do {
            BillDetailDto originDto = detailDtos.get(index);
            BigDecimal billDetailAmount = originDto.getAmounts().add(aggrAmt);
            if (billDetailAmount.compareTo(invLimitAmt) <= 0) {
                break;
            }
            BillDetailDto productDto = new BillDetailDto();
            BillDetailDto newProductDto = new BillDetailDto();
            if (originDto.getOriginalAmts() == null) {
                originDto.setOriginalAmts(originDto.getAmts());
                productDto.setOriginalAmts(originDto.getAmts());
                newProductDto.setOriginalAmts(originDto.getAmts());
            }
            if (hasDisLine) {
                int disLineIndex = index + 1;
                BillDetailDto orginDisDto = detailDtos.get(disLineIndex);
                BillDetailDto disDto = new BillDetailDto();
                BillDetailDto newDisDto = new BillDetailDto();
                splitLineByDis(configDto, originDto, productDto, newProductDto, orginDisDto, disDto, newDisDto, aggrAmt, invLimitAmt);
                detailDtos.set(index, productDto);
                BigDecimal disAmount = disDto.getAmounts();
                if (disAmount.compareTo(BigDecimal.ZERO) == 0) {
                    if (newProductDto.getAmounts().compareTo(BigDecimal.ZERO) == 0) {
                        break;
                    }
                    detailDtos.set(index + 1, newProductDto);
                    ++index;
                    if (newDisDto.getAmounts().compareTo(BigDecimal.ZERO) != 0) {
                        detailDtos.add(index + 1, newDisDto);
                        ++index;
                    }
                }
                else {
                    detailDtos.set(index + 1, disDto);
                    ++index;
                    if (newProductDto.getAmounts().compareTo(BigDecimal.ZERO) == 0) {
                        break;
                    }
                    detailDtos.add(index + 1, newProductDto);
                    ++index;
                    if (newDisDto.getAmounts().compareTo(BigDecimal.ZERO) != 0) {
                        detailDtos.add(index + 1, newDisDto);
                    }
                }
                allAmount = allAmount.subtract(productDto.getAmounts());
                allAmount = allAmount.subtract(disDto.getAmounts());
            }
            else {
                splitLine(configDto, originDto, productDto, newProductDto, aggrAmt, invLimitAmt);
                detailDtos.set(index, productDto);
                if (newProductDto.getAmounts().compareTo(BigDecimal.ZERO) == 0) {
                    break;
                }
                detailDtos.add(index + 1, newProductDto);
                ++index;
                allAmount = allAmount.subtract(productDto.getAmounts());
            }
            aggrAmt = BigDecimal.ZERO;
            ++splitAmountIndex;
            if (splitAmounts != null && !splitAmounts.isEmpty() && splitAmounts.size() > splitAmountIndex) {
                invLimitAmt = splitAmounts.get(splitAmountIndex);
            }
            else {
                invLimitAmt = configDto.getFinalLimitAmt();
            }
        } while (addNextProLineIsExpeed(detailDtos, index, invLimitAmt));
        BillDetailDto lastBillDetail = detailDtos.get(detailDtos.size() - 1);
        if (hasDisLine) {
            lastBillDetail = detailDtos.get(detailDtos.size() - 2);
        }
        Map<String, Integer> decimalMap = getDigitByRuleConfig(configDto);
        int amtNumber = decimalMap.get("amtNumber");
        int priceNumber = decimalMap.get("priceNumber");
        int includeTax = lastBillDetail.getIncludeTax();
        BigDecimal price = lastBillDetail.getPrice();
        BigDecimal amts = lastBillDetail.getAmts();
        BigDecimal amounts = lastBillDetail.getAmounts();
        BigDecimal priceInc = lastBillDetail.getPriceIncTax();
        BigDecimal amountsInc = lastBillDetail.getAmountsIncTax();
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            splitAmountIndex = 0;
            if (EnumType.YOrNEnum.YES.getValue() == includeTax && priceInc.multiply(amts).subtract(amountsInc).abs().compareTo(lineAmtErr) > 0) {
                if (splitGoodsType == EnumType.SplitGoodsTypeEnum.TWO.getValue()) {
                    amts = InvoiceSplitCoreService.calcUtilMethods.recursionAmtsCut(amountsInc, priceInc, amtNumber, configDto);
                    lastBillDetail.setAmts(amts);
                }
                else if (splitGoodsType == EnumType.SplitGoodsTypeEnum.THREE.getValue()) {
                    priceInc = InvoiceSplitCoreService.calcUtilMethods.recursionPrice(amountsInc, amts, priceNumber, configDto);
                    lastBillDetail.setPriceIncTax(priceInc);
                }
            }
            price = lastBillDetail.getPrice();
            amts = lastBillDetail.getAmts();
            amounts = lastBillDetail.getAmounts();
            splitAmountIndex = 0;
            if (price.multiply(amts).subtract(amounts).abs().compareTo(lineAmtErr) > 0) {
                if (splitGoodsType == EnumType.SplitGoodsTypeEnum.TWO.getValue()) {
                    amts = InvoiceSplitCoreService.calcUtilMethods.recursionAmtsCut(amounts, price, amtNumber, configDto);
                    lastBillDetail.setAmts(amts);
                }
                else if (splitGoodsType == EnumType.SplitGoodsTypeEnum.THREE.getValue()) {
                    price = InvoiceSplitCoreService.calcUtilMethods.recursionPrice(amounts, amts, priceNumber, configDto);
                    lastBillDetail.setPrice(price);
                }
            }
        }
        BigDecimal err = lastBillDetail.getAmounts().multiply(lastBillDetail.getTaxRate()).subtract(lastBillDetail.getTaxAmt());
        if (err.abs().compareTo(lineTaxAmtErr) > 0) {
            final BigDecimal adjustAmount = err.divide(new BigDecimal(new StringBuilder(String.valueOf(detailDtos.size())).toString()), 2, 0);
            for (final BillDetailDto billDetailDto : detailDtos) {
                if (!isDisLine(billDetailDto)) {
                    if (err.abs().compareTo(lineAmtErr) <= 0) {
                        break;
                    }
                    BigDecimal allowAdjust = billDetailDto.getAmounts().multiply(billDetailDto.getTaxRate()).subtract(billDetailDto.getTaxAmt()).abs().subtract(new BigDecimal("0.06")).abs().setScale(2, 1);
                    allowAdjust = ((adjustAmount.compareTo(allowAdjust) > 0) ? allowAdjust : adjustAmount);
                    billDetailDto.setTaxAmt(billDetailDto.getTaxAmt().subtract(allowAdjust));
                    lastBillDetail.setTaxAmt(lastBillDetail.getTaxAmt().add(allowAdjust));
                    err = err.subtract(allowAdjust);
                }
            }
        }
        if (hasDisLine) {
            final BillDetailDto lastBillDteailDis = detailDtos.get(detailDtos.size() - 1);
            BigDecimal disErr = lastBillDteailDis.getAmounts().multiply(lastBillDteailDis.getTaxRate()).subtract(lastBillDteailDis.getTaxAmt());
            if (disErr.abs().compareTo(lineTaxAmtErr) > 0) {
                final BigDecimal adjustAmount2 = disErr.divide(new BigDecimal(new StringBuilder(String.valueOf(detailDtos.size())).toString()), 2, 0);
                for (final BillDetailDto billDetailDto2 : detailDtos) {
                    if (isDisLine(billDetailDto2)) {
                        if (disErr.abs().compareTo(lineAmtErr) <= 0) {
                            break;
                        }
                        BigDecimal allowAdjust2 = billDetailDto2.getAmounts().multiply(billDetailDto2.getTaxRate()).subtract(billDetailDto2.getTaxAmt()).abs().subtract(new BigDecimal("0.06")).abs().setScale(2, 1);
                        allowAdjust2 = ((adjustAmount2.compareTo(allowAdjust2) > 0) ? allowAdjust2 : adjustAmount2);
                        billDetailDto2.setTaxAmt(billDetailDto2.getTaxAmt().subtract(allowAdjust2));
                        lastBillDteailDis.setTaxAmt(lastBillDteailDis.getTaxAmt().add(allowAdjust2));
                        disErr = disErr.subtract(allowAdjust2);
                    }
                }
            }
        }
        price = lastBillDetail.getPrice();
        amts = lastBillDetail.getAmts();
        amounts = lastBillDetail.getAmounts();
        priceInc = lastBillDetail.getPriceIncTax();
        amountsInc = lastBillDetail.getAmountsIncTax();
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal allAmounts = amounts;
            if (hasDisLine) {
                allAmounts = allAmounts.add(detailDtos.get(detailDtos.size() - 1).getAmounts());
            }
            if (allAmounts.compareTo(invLimitAmt) > 0 && priceNumber < 15) {
                configDto.setPriceNumber(priceNumber + 1);
                if (configDto.getPriceNumberType() != 1) {
                    detailDtos = goodsLineSplit(originDetailDtos, configDto, originAggrAmt, hasDisLine, allAmount, splitAmounts);
                }
                configDto.setPriceNumber(priceNumber);
                return detailDtos;
            }
            if (priceInc.multiply(amts).subtract(amountsInc).abs().compareTo(lineAmtErr) > 0 && priceNumber < 15) {
                configDto.setPriceNumber(priceNumber + 1);
                if (configDto.getPriceNumberType() != 1) {
                    detailDtos = goodsLineSplit(originDetailDtos, configDto, originAggrAmt, hasDisLine, allAmount, splitAmounts);
                }
                configDto.setPriceNumber(priceNumber);
                return detailDtos;
            }
            if (price.multiply(amts).subtract(amounts).abs().compareTo(lineAmtErr) > 0 && priceNumber < 15) {
                configDto.setPriceNumber(priceNumber + 1);
                if (configDto.getPriceNumberType() != 1) {
                    detailDtos = goodsLineSplit(originDetailDtos, configDto, originAggrAmt, hasDisLine, allAmount, splitAmounts);
                }
                configDto.setPriceNumber(priceNumber);
                return detailDtos;
            }
        }
        return detailDtos;
    }
    
    private static Boolean addNextProLineIsExpeed(final List<BillDetailDto> detailDtos, final int curIndex, final BigDecimal invLimitAmt) {
        final BillDetailDto lineDto = detailDtos.get(curIndex);
        BigDecimal countNextAmount = BigDecimal.ZERO.add(lineDto.getAmounts());
        final int j = curIndex + 1;
        if (j < detailDtos.size()) {
            final BillDetailDto disLineDto = detailDtos.get(j);
            countNextAmount = countNextAmount.add(disLineDto.getAmounts());
        }
        if (countNextAmount.compareTo(invLimitAmt) > 0) {
            return true;
        }
        return false;
    }
    
    private static void splitLineByDis(final SmruleConfigDto configDto, final BillDetailDto orginDto, final BillDetailDto productDto, final BillDetailDto newProductDto, final BillDetailDto orginDisDto, final BillDetailDto disDto, final BillDetailDto newdisDto, final BigDecimal aggrAmt, final BigDecimal invLimitAmt) throws IllegalAccessException, InvocationTargetException {
        final BigDecimal dec = orginDto.getTaxDeduction();
        final BigDecimal oAmounts = orginDto.getAmounts();
        final BigDecimal oDisAmounts = orginDisDto.getAmounts();
        final BigDecimal lineAmtErr = configDto.getLineAmountErr();
        final BigDecimal itemAmt = invLimitAmt.subtract(aggrAmt);
        BigDecimal calDisAmt = null;
        BigDecimal usedAmt = null;
        if (itemAmt.compareTo(invLimitAmt) > 0) {
            calDisAmt = oDisAmounts.multiply(invLimitAmt).divide(oAmounts, 2, 4);
            usedAmt = invLimitAmt.subtract(calDisAmt);
        }
        else {
            final BigDecimal itemAmtGroup = oAmounts.add(oDisAmounts);
            calDisAmt = oDisAmounts.multiply(itemAmt).divide(itemAmtGroup, 2, 4);
            usedAmt = itemAmt.subtract(calDisAmt);
        }
        final int includeTax = orginDto.getIncludeTax();
        final Map<String, Integer> decimalMap = getDigitByRuleConfig(configDto);
        final int amtNumber = decimalMap.get("amtNumber");
        final int priceNumber = decimalMap.get("priceNumber");
        BeanUtils.copyProperties(productDto, orginDto);
        productDto.setAmounts(usedAmt);
        final BigDecimal taxRate = productDto.getTaxRate();
        BigDecimal amounts = productDto.getAmounts();
        BigDecimal price = productDto.getPrice();
        BigDecimal price2 = productDto.getPrice();
        BigDecimal priceInc = productDto.getPriceIncTax();
        BigDecimal priceInc2 = productDto.getPriceIncTax();
        BigDecimal amts = null;
        BigDecimal amts2 = null;
        BigDecimal taxAmt = null;
        BigDecimal taxAmt2 = null;
        BigDecimal amounts2 = null;
        BigDecimal amountsInc = null;
        BigDecimal amountsInc2 = null;
        BeanUtils.copyProperties(newProductDto, orginDto);
        final BigDecimal tmpAmountsInc = orginDto.getAmountsIncTax();
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            price = price.setScale(priceNumber, 4);
            if (price.compareTo(BigDecimal.ZERO) == 0) {
                price = price2;
            }
            else {
                price2 = price2.setScale(priceNumber, 4);
            }
            priceInc = priceInc.setScale(priceNumber, 4);
            if (priceInc.compareTo(BigDecimal.ZERO) == 0) {
                priceInc = priceInc2;
            }
            else {
                priceInc2 = priceInc2.setScale(priceNumber, 4);
            }
            amts = InvoiceSplitCoreService.calcUtilMethods.recursionAmtsCut(amounts, price, amtNumber, configDto);
            amts = amts.setScale(amtNumber, 3);
            if (amts.compareTo(BigDecimal.ZERO) == 0) {
                amts = InvoiceSplitCoreService.calcUtilMethods.recursionAmtsCut(amounts, price, amtNumber, configDto);
            }
            if (amts.compareTo(BigDecimal.ONE) <= 0 && price.compareTo(invLimitAmt) < 0 && amts.setScale(amtNumber, 3).compareTo(BigDecimal.ZERO) == 0) {
                BeanUtils.copyProperties(productDto, orginDto);
                newProductDto.setAmounts(BigDecimal.ZERO);
                BeanUtils.copyProperties(disDto, orginDisDto);
                newdisDto.setAmounts(BigDecimal.ZERO);
                return;
            }
            amts2 = orginDto.getAmts().subtract(amts);
            if (amts2.compareTo(BigDecimal.ZERO) < 0) {
                configDto.setPriceNumber(priceNumber + 1);
                if (configDto.getPriceNumber() > 15) {
                    BeanUtils.copyProperties(productDto, orginDto);
                    newProductDto.setAmounts(BigDecimal.ZERO);
                    BeanUtils.copyProperties(disDto, orginDisDto);
                    newdisDto.setAmounts(BigDecimal.ZERO);
                    return;
                }
                splitLineByDis(configDto, orginDto, productDto, newProductDto, orginDisDto, disDto, newdisDto, aggrAmt, invLimitAmt);
                return;
            }
            else {
                amounts = InvoiceSplitCoreService.calcUtilMethods.recursionAmounts(amts, price, 2, configDto);
                amounts2 = orginDto.getAmounts().subtract(amounts);
                amountsInc = InvoiceSplitCoreService.calcUtilMethods.recursionAmounts(amts, priceInc, 2, configDto);
                amountsInc2 = orginDto.getAmountsIncTax().subtract(amountsInc);
                taxAmt = amountsInc.subtract(amounts);
                taxAmt2 = orginDto.getTaxAmt().subtract(taxAmt);
                if (amounts.compareTo(BigDecimal.ZERO) <= 0 || amounts2.compareTo(BigDecimal.ZERO) < 0 || amountsInc.compareTo(BigDecimal.ZERO) <= 0 || amountsInc2.compareTo(BigDecimal.ZERO) < 0) {
                    BeanUtils.copyProperties(productDto, orginDto);
                    newProductDto.setAmounts(BigDecimal.ZERO);
                    BeanUtils.copyProperties(disDto, orginDisDto);
                    newdisDto.setAmounts(BigDecimal.ZERO);
                    return;
                }
                if (amounts.multiply(taxRate).subtract(taxAmt).abs().compareTo(configDto.getLineTaxAmtErr()) > 0) {
                    taxAmt = InvoiceSplitCoreService.calcUtilMethods.calcTaxAmtByNoTaxMoneyDec(amounts, dec, taxRate, 2);
                    taxAmt2 = orginDto.getTaxAmt().subtract(taxAmt);
                }
                if (taxRate.compareTo(BigDecimal.ZERO) == 0) {
                    taxAmt = orginDto.getTaxAmt().multiply(amounts).divide(orginDto.getAmounts(), 2, 4);
                    taxAmt2 = orginDto.getTaxAmt().subtract(taxAmt);
                    amountsInc = amounts.add(taxAmt);
                    amountsInc2 = orginDto.getAmountsIncTax().subtract(amountsInc);
                    if (EnumType.YOrNEnum.YES.getValue() == includeTax && priceInc.multiply(amts).subtract(amountsInc).abs().compareTo(lineAmtErr) > 0) {
                        amountsInc = InvoiceSplitCoreService.calcUtilMethods.recursionAmounts(amts, priceInc, 2, configDto);
                        amountsInc2 = orginDto.getAmountsIncTax().subtract(amountsInc);
                    }
                }
                productDto.setAmts(amts);
                productDto.setPrice(price);
                productDto.setPriceIncTax(priceInc);
                newProductDto.setAmts(amts2);
                newProductDto.setPrice(price2);
                newProductDto.setPriceIncTax(priceInc2);
            }
        }
        else {
            amounts = usedAmt.setScale(2, 4);
            amounts2 = oAmounts.subtract(amounts).setScale(2, 4);
            taxAmt = InvoiceSplitCoreService.calcUtilMethods.calcTaxAmtByNoTaxMoneyDec(amounts, dec, taxRate, 2);
            taxAmt2 = orginDto.getTaxAmt().subtract(taxAmt);
            amountsInc = amounts.add(taxAmt);
            amountsInc2 = tmpAmountsInc.subtract(amountsInc).setScale(2, 4);
        }
        productDto.setAmounts(amounts);
        productDto.setAmountsIncTax(amountsInc);
        productDto.setTaxAmt(taxAmt);
        productDto.setSplitSign(1);
        newProductDto.setAmounts(amounts2);
        newProductDto.setAmountsIncTax(amountsInc2);
        newProductDto.setTaxAmt(taxAmt2);
        newProductDto.setSplitSign(1);
        BeanUtils.copyProperties(disDto, orginDisDto);
        final BigDecimal disTaxRate = disDto.getTaxRate();
        BigDecimal disAmtount = itemAmt.subtract(amounts);
        disAmtount = ((disAmtount.compareTo(BigDecimal.ZERO) >= 0) ? calDisAmt : disAmtount);
        final BigDecimal disTaxAmt = disAmtount.multiply(disTaxRate).setScale(2, 4);
        disDto.setAmounts(disAmtount);
        disDto.setTaxAmt(disTaxAmt);
        final BigDecimal disAmtInc = disAmtount.add(disTaxAmt);
        disDto.setAmountsIncTax(disAmtInc);
        disDto.setSplitSign(1);
        BeanUtils.copyProperties(newdisDto, orginDisDto);
        final BigDecimal disAmt1 = orginDisDto.getAmounts().subtract(disAmtount).setScale(2, 4);
        final BigDecimal disTaxAmt2 = orginDisDto.getTaxAmt().subtract(disTaxAmt).setScale(2, 4);
        newdisDto.setAmounts(disAmt1);
        newdisDto.setTaxAmt(disTaxAmt2);
        final BigDecimal disAmtInc2 = orginDisDto.getAmountsIncTax().subtract(disAmtInc);
        newdisDto.setAmountsIncTax(disAmtInc2);
        newdisDto.setSplitSign(1);
        BillDetailIdSplit(orginDto, productDto, newProductDto, orginDisDto, disDto, newdisDto, configDto);
    }
    
    public static BigDecimal getTaxAmtByIncTax(final int includeTax, final BigDecimal amountInc, final BigDecimal amount, final BigDecimal dec, final BigDecimal taxRate) {
        if (EnumType.YOrNEnum.YES.getValue() == includeTax) {
            return InvoiceSplitCoreService.calcUtilMethods.calcTaxAmtByTaxMoneyDec(amountInc, dec, taxRate, 2);
        }
        return InvoiceSplitCoreService.calcUtilMethods.calcTaxAmtByNoTaxMoneyDec(amount, dec, taxRate, 2);
    }
    
    /* 
     * 该方法的作用是根据拆分规则将原始单据明细信息拆分成两个单据明细信息，分别为 productDto 和 newProductDto。
     * 在拆分过程中，会根据已拆分的金额总和和单据拆分限额计算出本次拆分的金额，并更新 productDto 和 newProductDto 的相关属性值。
     * 如果无法拆分，则直接将 productDto 的金额设置为已拆分的金额总和，将 newProductDto 的金额设置为 0。
     * configDto：类型为 SmruleConfigDto，表示拆分规则配置信息。
     * orginDto：类型为 BillDetailDto，表示原始单据明细信息。
     * productDto：类型为 BillDetailDto，表示拆分后的单据明细信息。
     * newProductDto：类型为 BillDetailDto，表示拆分后的新单据明细信息。
     * aggrAmt：类型为 BigDecimal，表示已拆分的金额总和。
     * invLimitAmt：类型为 BigDecimal，表示单据拆分限额。
     */
    private static void splitLine(SmruleConfigDto configDto, BillDetailDto orginDto, BillDetailDto productDto, 
                                BillDetailDto newProductDto, BigDecimal aggrAmt, BigDecimal invLimitAmt) throws IllegalAccessException, InvocationTargetException {
        BigDecimal dec = orginDto.getTaxDeduction();
        int includeTax = orginDto.getIncludeTax();
        BigDecimal lineAmtErr = configDto.getLineAmountErr();
        Map<String, Integer> decimalMap = getDigitByRuleConfig(configDto);
        int amtNumber = decimalMap.get("amtNumber");
        int priceNumber = decimalMap.get("priceNumber");
        BigDecimal usedAmt = invLimitAmt.subtract(aggrAmt);
        BeanUtils.copyProperties(productDto, orginDto);
        productDto.setAmounts(usedAmt);
        BigDecimal taxRate = productDto.getTaxRate();
        BigDecimal amounts = productDto.getAmounts();
        BigDecimal price = productDto.getPrice();
        BigDecimal price2 = productDto.getPrice();
        BigDecimal priceInc = productDto.getPriceIncTax();
        BigDecimal priceInc2 = productDto.getPriceIncTax();
        BigDecimal amts = null;
        BigDecimal amts2 = null;
        BigDecimal taxAmt = null;
        BigDecimal taxAmt2 = null;
        BigDecimal amounts2 = null;
        BigDecimal amountsInc = null;
        BigDecimal amountsInc2 = null;
        BeanUtils.copyProperties(newProductDto, orginDto);
        BigDecimal tmpAmountsInc = orginDto.getAmountsIncTax();
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            priceInc = priceInc.setScale(priceNumber, 4);
            if (priceInc.compareTo(BigDecimal.ZERO) == 0) {
                priceInc = priceInc2;
            }
            else {
                priceInc2 = priceInc2.setScale(priceNumber, 4);
            }
            price = price.setScale(priceNumber, 4);
            if (price.compareTo(BigDecimal.ZERO) == 0) {
                price = price2;
            }
            else {
                price2 = price2.setScale(priceNumber, 4);
            }
            amts = InvoiceSplitCoreService.calcUtilMethods.recursionAmtsCut(amounts, price, amtNumber, configDto);
            amts = amts.setScale(amtNumber, 3);
            if (amts.compareTo(BigDecimal.ZERO) == 0) {
                amts = InvoiceSplitCoreService.calcUtilMethods.recursionAmtsCut(amounts, price, amtNumber, configDto);
            }
            if (amts.compareTo(BigDecimal.ONE) <= 0 && price.compareTo(invLimitAmt) < 0 && amts.setScale(amtNumber, 3).compareTo(BigDecimal.ZERO) == 0) {
                BeanUtils.copyProperties(productDto, orginDto);
                newProductDto.setAmounts(BigDecimal.ZERO);
                return;
            }
            amts2 = orginDto.getAmts().subtract(amts);
            if (amts2.compareTo(BigDecimal.ZERO) < 0) {
                configDto.setPriceNumber(priceNumber + 1);
                if (configDto.getPriceNumber() > 15) {
                    BeanUtils.copyProperties(productDto, orginDto);
                    newProductDto.setAmounts(BigDecimal.ZERO);
                    return;
                }
                splitLine(configDto, orginDto, productDto, newProductDto, aggrAmt, invLimitAmt);
                return;
            }
            else {
                amounts = InvoiceSplitCoreService.calcUtilMethods.recursionAmounts(amts, price, 2, configDto);
                amounts2 = orginDto.getAmounts().subtract(amounts);
                amountsInc = InvoiceSplitCoreService.calcUtilMethods.recursionAmounts(amts, priceInc, 2, configDto);
                amountsInc2 = orginDto.getAmountsIncTax().subtract(amountsInc);
                taxAmt = amountsInc.subtract(amounts);
                taxAmt2 = orginDto.getTaxAmt().subtract(taxAmt);
                if (amounts.compareTo(BigDecimal.ZERO) <= 0 || amounts2.compareTo(BigDecimal.ZERO) < 0 || amountsInc.compareTo(BigDecimal.ZERO) <= 0 || amountsInc2.compareTo(BigDecimal.ZERO) < 0) {
                    BeanUtils.copyProperties(productDto, orginDto);
                    newProductDto.setAmounts(BigDecimal.ZERO);
                    return;
                }
                if (amounts.multiply(taxRate).subtract(taxAmt).abs().compareTo(configDto.getLineTaxAmtErr()) > 0) {
                    taxAmt = InvoiceSplitCoreService.calcUtilMethods.calcTaxAmtByNoTaxMoneyDec(amounts, dec, taxRate, 2);
                    taxAmt2 = orginDto.getTaxAmt().subtract(taxAmt);
                }
                if (taxRate.compareTo(BigDecimal.ZERO) == 0) {
                    taxAmt = orginDto.getTaxAmt().multiply(amounts).divide(orginDto.getAmounts(), 2, 4);
                    taxAmt2 = orginDto.getTaxAmt().subtract(taxAmt);
                    amountsInc = amounts.add(taxAmt);
                    amountsInc2 = orginDto.getAmountsIncTax().subtract(amountsInc);
                    if (EnumType.YOrNEnum.YES.getValue() == includeTax && priceInc.multiply(amts).subtract(amountsInc).abs().compareTo(lineAmtErr) > 0) {
                        amountsInc = InvoiceSplitCoreService.calcUtilMethods.recursionAmounts(amts, priceInc, 2, configDto);
                        amountsInc2 = orginDto.getAmountsIncTax().subtract(amountsInc);
                    }
                }
                productDto.setAmts(amts);
                productDto.setPrice(price);
                productDto.setPriceIncTax(priceInc);
                newProductDto.setAmts(amts2);
                newProductDto.setPrice(price2);
                newProductDto.setPriceIncTax(priceInc2);
            }
        }
        else {
            final BigDecimal oAmounts = orginDto.getAmounts();
            amounts = usedAmt.setScale(2, 3);
            amounts2 = oAmounts.subtract(amounts).setScale(2, 3);
            taxAmt = InvoiceSplitCoreService.calcUtilMethods.calcTaxAmtByNoTaxMoneyDec(amounts, dec, taxRate, 2);
            taxAmt2 = orginDto.getTaxAmt().subtract(taxAmt);
            amountsInc = amounts.add(taxAmt);
            amountsInc2 = tmpAmountsInc.subtract(amountsInc).setScale(2, 4);
        }
        productDto.setAmounts(amounts);
        productDto.setAmountsIncTax(amountsInc);
        productDto.setTaxAmt(taxAmt);
        productDto.setSplitSign(1);
        newProductDto.setAmounts(amounts2);
        newProductDto.setAmountsIncTax(amountsInc2);
        newProductDto.setTaxAmt(taxAmt2);
        newProductDto.setSplitSign(1);
        BillDetailIdSplit(orginDto, productDto, newProductDto, null, null, null, configDto);
    }
    
    public static Map<String, Integer> getDigitByRuleConfig(final SmruleConfigDto configDto) {
        final Map<String, Integer> retMap = new HashMap<String, Integer>(1);
        final Integer amtNumberType = configDto.getAmtNumberType();
        final Integer amtNumber = configDto.getAmtNumber();
        final Integer priceNumberType = configDto.getPriceNumberType();
        final Integer priceNumber = configDto.getPriceNumber();
        int digit = SmserConstant.ONE;
        if (EnumType.NumberTypeEnum.ZERO.getValue() == (int)priceNumberType) {
            digit = priceNumber;
        }
        else if (EnumType.NumberTypeEnum.ONE.getValue() == (int)priceNumberType) {
            digit = 0;
        }
        else if (EnumType.NumberTypeEnum.TWO.getValue() == (int)priceNumberType) {
            digit = priceNumber;
        }
        retMap.put("priceNumber", digit);
        if (EnumType.NumberTypeEnum.ZERO.getValue() == (int)amtNumberType) {
            digit = amtNumber;
        }
        else if (EnumType.NumberTypeEnum.ONE.getValue() == (int)amtNumberType) {
            digit = 0;
        }
        else if (EnumType.NumberTypeEnum.TWO.getValue() == (int)amtNumberType) {
            digit = amtNumber;
        }
        retMap.put("amtNumber", digit);
        return retMap;
    }
    
    public static Boolean isDisLine(final BillDetailDto detailDto) {
        final Integer lineProperty = detailDto.getLineProperty();
        final BigDecimal amounts = detailDto.getAmounts();
        Boolean isDisline = false;
        if (EnumType.LinePropertyEnum.FOUR.getValue().compareTo(lineProperty) == 0 && amounts.compareTo(BigDecimal.ZERO) < 0) {
            isDisline = true;
        }
        return isDisline;
    }
    
    public static void BillDetailIdSplit(BillDetailDto originDto, BillDetailDto productDto, BillDetailDto newProductDto,
                                         BillDetailDto originDisDto, BillDetailDto disDto, BillDetailDto newdisDto,
                                         SmruleConfigDto configDto) {
        final Set<BillDetailIdDto> orginDetailIdSet = originDto.getDetailIdSet();
        final Iterator<BillDetailIdDto> it = orginDetailIdSet.iterator();
        final Set<BillDetailIdDto> productDetailIdSet = new LinkedHashSet<BillDetailIdDto>();
        BigDecimal sumAmount = BigDecimal.ZERO;
        while (it.hasNext()) {
            final BillDetailIdDto billDetailIdDto = it.next();
            sumAmount = sumAmount.add(billDetailIdDto.getAmounts());
            if (sumAmount.compareTo(productDto.getAmountsByTax()) > 0) {
                BigDecimal splitAomunt2 = sumAmount.subtract(productDto.getAmountsByTax());
                BigDecimal splitAomunt3 = billDetailIdDto.getAmounts().subtract(splitAomunt2);
                BigDecimal amt2 = InvoiceSplitCoreService.calcUtilMethods.recursionAmtsCut(splitAomunt2, billDetailIdDto.getPrice(), configDto.getAmtNumber(), configDto);
                BigDecimal amt3 = billDetailIdDto.getAmts().subtract(amt2);
                BillDetailIdDto splitBillDetailId1 = new BillDetailIdDto(billDetailIdDto.getBillNO(),
                    billDetailIdDto.getBillDetailNO(), splitAomunt3, billDetailIdDto.getPrice(), amt3, productDto.getTaxAmt());
                BillDetailIdDto splitBillDetailId2 = new BillDetailIdDto(billDetailIdDto.getBillNO(),
                    billDetailIdDto.getBillDetailNO(), splitAomunt2, billDetailIdDto.getPrice(), amt2, newProductDto.getTaxAmt());
                productDetailIdSet.add(splitBillDetailId1);
                it.remove();
                orginDetailIdSet.add(splitBillDetailId2);
                break;
            }
            if (sumAmount.compareTo(productDto.getAmountsByTax()) == 0) {
                productDetailIdSet.add(billDetailIdDto);
                it.remove();
                break;
            }
            productDetailIdSet.add(billDetailIdDto);
            it.remove();
        }
        productDto.setDetailIdSet(productDetailIdSet);
        newProductDto.setDetailIdSet(orginDetailIdSet);
        if (originDisDto != null) {
            Set<BillDetailIdDto> orginDisDetailIdSet = originDisDto.getDetailIdSet();
            Iterator<BillDetailIdDto> disIt = orginDisDetailIdSet.iterator();
            Set<BillDetailIdDto> disProductDetailIdSet = new LinkedHashSet<BillDetailIdDto>();
            BigDecimal sumDisAmount = BigDecimal.ZERO;
            while (disIt.hasNext()) {
                BillDetailIdDto billDetailIdDto2 = disIt.next();
                sumDisAmount = sumDisAmount.add(billDetailIdDto2.getAmounts());
                if (sumDisAmount.compareTo(disDto.getAmountsByTax()) < 0) {
                    BigDecimal splitAomunt4 = sumDisAmount.subtract(disDto.getAmountsByTax());
                    BigDecimal splitAomunt5 = billDetailIdDto2.getAmounts().subtract(splitAomunt4);
                    BillDetailIdDto splitBillDetailId3 = new BillDetailIdDto(billDetailIdDto2.getBillNO(),
                        billDetailIdDto2.getBillDetailNO(), splitAomunt5, billDetailIdDto2.getPrice(),
                        billDetailIdDto2.getAmts(), disDto.getTaxAmt());
                    BillDetailIdDto splitBillDetailId4 = new BillDetailIdDto(billDetailIdDto2.getBillNO(),
                        billDetailIdDto2.getBillDetailNO(), splitAomunt4, billDetailIdDto2.getPrice(),
                        billDetailIdDto2.getAmts(), newdisDto.getTaxAmt());
                    disProductDetailIdSet.add(splitBillDetailId3);
                    disIt.remove();
                    orginDisDetailIdSet.add(splitBillDetailId4);
                    break;
                }
                if (sumDisAmount.compareTo(disDto.getAmountsByTax()) == 0) {
                    disProductDetailIdSet.add(billDetailIdDto2);
                    disIt.remove();
                    break;
                }
                disProductDetailIdSet.add(billDetailIdDto2);
                disIt.remove();
            }
            disDto.setDetailIdSet(disProductDetailIdSet);
            newdisDto.setDetailIdSet(orginDisDetailIdSet);
        }
    }
}
