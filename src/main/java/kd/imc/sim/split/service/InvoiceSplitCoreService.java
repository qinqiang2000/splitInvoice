package kd.imc.sim.split.service;

import kd.imc.sim.common.constant.InvoiceConstant;
import kd.imc.sim.split.dto.BillDetailDto;
import kd.imc.sim.split.dto.BillDetailIdDto;
import kd.imc.sim.split.dto.SmruleConfigDto;
import kd.imc.sim.split.enums.EnumType.LinePropertyEnum;
import kd.imc.sim.split.enums.EnumType.NumberTypeEnum;
import kd.imc.sim.split.enums.EnumType.SplitGoodsTypeEnum;
import kd.imc.sim.split.enums.EnumType.YOrNEnum;
import kd.imc.sim.split.methods.BackCalcUtilMethods;
import kd.imc.sim.split.utils.ComUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;

import static kd.imc.sim.split.enums.EnumType.*;

public class InvoiceSplitCoreService {
    //private static Log LOG = LogFactory.getLog(InvoiceSplitCoreService.class);
    private static Logger LOG = LoggerFactory.getLogger(InvoiceSplitCoreService.class);
    private static BackCalcUtilMethods calcUtilMethods = new BackCalcUtilMethods();

    public static void dealDetailByRule(BillDetailDto curDto, SmruleConfigDto configDto, List<BillDetailDto> billDetailDtos, int curIndex) {
        Map<String, Integer> decimalMap = getDigitByRuleConfig(configDto);
        int amtNumber = decimalMap.get("amtNumber");
        int priceNumber = decimalMap.get("priceNumber");
        BigDecimal amounts = curDto.getAmounts();
        BigDecimal amountsInc = curDto.getAmountsIncTax();
        BigDecimal taxAmt = curDto.getTaxAmt();
        BigDecimal amts = curDto.getAmts();
        BigDecimal price = curDto.getPrice();
        BigDecimal priceInc = curDto.getPriceIncTax();
        int includeTax = curDto.getIncludeTax();
        BigDecimal tmpAmountsInc = curDto.getAmountsIncTax();
        BigDecimal lineAmountErr = configDto.getLineAmountErr();
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            amts = amts.setScale(amtNumber, 4);
            amountsInc = amountsInc.setScale(2, 4);
            amounts = amounts.setScale(2, 4);
            if (YOrNEnum.YES.getValue() == includeTax) {
                priceInc = priceInc.setScale(priceNumber, 4);
                if (amts.multiply(priceInc).subtract(amountsInc).abs().compareTo(lineAmountErr) > 0) {
                    priceInc = calcUtilMethods.recursionPrice(amountsInc, amts, priceNumber, configDto);
                }

                if (amts.multiply(price).subtract(amounts).abs().compareTo(lineAmountErr) > 0) {
                    price = calcUtilMethods.recursionPrice(amounts, amts, priceNumber, configDto);
                }
            } else {
                price = price.setScale(priceNumber, 4);
                if (amts.multiply(price).subtract(amounts).abs().compareTo(lineAmountErr) > 0) {
                    price = calcUtilMethods.recursionPrice(amounts, amts, 0, configDto);
                }

                if (amts.multiply(priceInc).subtract(amountsInc).abs().compareTo(lineAmountErr) > 0) {
                    priceInc = calcUtilMethods.recursionPrice(amountsInc, amts, 0, configDto);
                }
            }

            curDto.setAmts(amts);
            curDto.setPrice(price);
            curDto.setPriceIncTax(priceInc);
        } else {
            amountsInc = amountsInc.setScale(2, 4);
            amounts = tmpAmountsInc.subtract(taxAmt).setScale(2, 4);
        }

        curDto.setTaxAmt(taxAmt);
        curDto.setAmounts(amounts);
        curDto.setAmountsIncTax(amountsInc);
        int j = curIndex + 1;
        if (j < billDetailDtos.size()) {
            BillDetailDto disDto = billDetailDtos.get(j);
            if (ComUtil.isDisLine(disDto)) {
                BigDecimal disAmt = disDto.getAmounts();
                BigDecimal disAmtInc = disDto.getAmountsIncTax();
                BigDecimal oDisAmtInc = disDto.getAmountsIncTax();
                BigDecimal disTaxAmt;
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
     * 该函数的作用是递归的将明细列表按照拆分规则进行拆分，并返回拆分后的明细列表
     * detailDtos：明细列表；
     * configDto：拆分规则配置；
     * aggrAmt：已累计拆分的总金额（是入参，也是出参）；
     * hasDisLine：是否有折扣行；
     * allAmount：单据总金额（是入参，也是出参）；
     */
    public static List<BillDetailDto> goodsLineSplit(List<BillDetailDto> detailDtos, SmruleConfigDto configDto, BigDecimal aggrAmt, boolean hasDisLine, BigDecimal allAmount) throws IllegalAccessException, InvocationTargetException {
        int splitGoodsType = configDto.getSplitGoodsWithNumber();
        BigDecimal invLimitAmt = configDto.getFinalLimitAmt();
        BigDecimal lineTaxAmtErr = configDto.getLineTaxAmtErr();
        BigDecimal lineAmtErr = configDto.getLineAmountErr();
        List<BillDetailDto> originDetailDtos = new ArrayList<>();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(detailDtos);
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            originDetailDtos = (List<BillDetailDto>) ois.readObject();
        } catch (Exception e) {
            LOG.error("商品拆分时，备份数据异常", e);
        }

        BigDecimal originAggrAmt = aggrAmt;
        int index = 0;
        allAmount = allAmount.subtract(aggrAmt);

        BillDetailDto lastBillDetail;
        int includeTax;
        BigDecimal priceInc;

        /* 获取最后一行明细，如果有折扣行，则获取倒数第二行明细。
        根据拆分规则配置获取金额和价格的小数位数。
        计算最后一行明细的金额，如果金额小于等于拆分规则配置中的最终限制金额，则跳出循环。
        如果最后一行明细没有原始金额，则将其原始金额设置为其金额。
        如果有折扣行，则拆分明细行和折扣行，否则只拆分明细行。
        将拆分后的明细行和折扣行（如果有）插入到明细列表中。
        更新已累计拆分的总金额和最终限制金额。
        如果还有下一行明细，则继续拆分，否则返回拆分后的明细列表。
        需要注意的是，函数中调用了其他函数 splitLineByDis 和 splitLine，这两个函数分别用于拆分明细行和折扣行，具体实现可以参考这两个函数的代码。
         */
        do {
            lastBillDetail = detailDtos.get(index);
            BigDecimal billDetailAmount = lastBillDetail.getAmounts().add(aggrAmt);
            if (billDetailAmount.compareTo(invLimitAmt) <= 0) {
                break;
            }

            BillDetailDto productDto = new BillDetailDto();
            BillDetailDto newProductDto = new BillDetailDto();
            if (lastBillDetail.getOriginalAmts() == null) {
                lastBillDetail.setOriginalAmts(lastBillDetail.getAmts());
                productDto.setOriginalAmts(lastBillDetail.getAmts());
                newProductDto.setOriginalAmts(lastBillDetail.getAmts());
            }

            if (hasDisLine) {
                includeTax = index + 1;
                BillDetailDto orginDisDto = detailDtos.get(includeTax);
                BillDetailDto disDto = new BillDetailDto();
                BillDetailDto newDisDto = new BillDetailDto();
                splitLineByDis(configDto, lastBillDetail, productDto, newProductDto, orginDisDto, disDto, newDisDto, aggrAmt, invLimitAmt);
                detailDtos.set(index, productDto);
                priceInc = disDto.getAmounts();
                if (priceInc.compareTo(BigDecimal.ZERO) == 0) {
                    if (newProductDto.getAmounts().compareTo(BigDecimal.ZERO) == 0) {
                        break;
                    }

                    detailDtos.set(index + 1, newProductDto);
                    ++index;
                    if (newDisDto.getAmounts().compareTo(BigDecimal.ZERO) != 0) {
                        detailDtos.add(index + 1, newDisDto);
                        ++index;
                    }
                } else {
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
            } else {
                splitLine(configDto, lastBillDetail, productDto, newProductDto, aggrAmt, invLimitAmt);
                detailDtos.set(index, productDto);
                if (newProductDto.getAmounts().compareTo(BigDecimal.ZERO) == 0) {
                    break;
                }

                detailDtos.add(index + 1, newProductDto);
                ++index;
                allAmount = allAmount.subtract(productDto.getAmounts());
            }

            aggrAmt = BigDecimal.ZERO;
            invLimitAmt = configDto.getFinalLimitAmt();
        } while (addNextProLineIsExpeed(detailDtos, index, invLimitAmt));

        lastBillDetail = detailDtos.get(detailDtos.size() - 1);
        if (hasDisLine) {
            lastBillDetail = detailDtos.get(detailDtos.size() - 2);
        }

        Map<String, Integer> decimalMap = getDigitByRuleConfig(configDto);
        int amtNumber = decimalMap.get("amtNumber");
        int priceNumber = decimalMap.get("priceNumber");
        includeTax = lastBillDetail.getIncludeTax();
        BigDecimal price = lastBillDetail.getPrice();
        BigDecimal amts = lastBillDetail.getAmts();
        BigDecimal amounts;
        priceInc = lastBillDetail.getPriceIncTax();
        BigDecimal amountsInc = lastBillDetail.getAmountsIncTax();
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            if (YOrNEnum.YES.getValue() == includeTax && priceInc.multiply(amts).subtract(amountsInc).abs().compareTo(lineAmtErr) > 0) {
                if (splitGoodsType == SplitGoodsTypeEnum.TWO.getValue()) {
                    amts = calcUtilMethods.recursionAmtsCut(amountsInc, priceInc, amtNumber, configDto);
                    lastBillDetail.setAmts(amts);
                } else if (splitGoodsType == SplitGoodsTypeEnum.THREE.getValue()) {
                    priceInc = calcUtilMethods.recursionPrice(amountsInc, amts, priceNumber, configDto);
                    lastBillDetail.setPriceIncTax(priceInc);
                }
            }

            price = lastBillDetail.getPrice();
            amts = lastBillDetail.getAmts();
            amounts = lastBillDetail.getAmounts();
            if (price.multiply(amts).subtract(amounts).abs().compareTo(lineAmtErr) > 0) {
                if (splitGoodsType == SplitGoodsTypeEnum.TWO.getValue()) {
                    amts = calcUtilMethods.recursionAmtsCut(amounts, price, amtNumber, configDto);
                    lastBillDetail.setAmts(amts);
                } else if (splitGoodsType == SplitGoodsTypeEnum.THREE.getValue()) {
                    price = calcUtilMethods.recursionPrice(amounts, amts, priceNumber, configDto);
                    lastBillDetail.setPrice(price);
                }
            }
        }

        BigDecimal err = lastBillDetail.getAmounts().multiply(lastBillDetail.getTaxRate()).subtract(lastBillDetail.getTaxAmt());
        BigDecimal allAmounts;
        if (err.abs().compareTo(lineTaxAmtErr) > 0) {
            allAmounts = err.divide(new BigDecimal(String.valueOf(detailDtos.size())), 2, 0);

            for (BillDetailDto billDetailDto : detailDtos) {
                if (!isDisLine(billDetailDto)) {
                    if (err.abs().compareTo(lineAmtErr) <= 0) {
                        break;
                    }

                    BigDecimal allowAdjust = billDetailDto.getAmounts().multiply(billDetailDto.getTaxRate()).subtract(billDetailDto.getTaxAmt()).abs().subtract(new BigDecimal("0.06")).abs().setScale(2, 1);
                    allowAdjust = allAmounts.compareTo(allowAdjust) > 0 ? allowAdjust : allAmounts;
                    billDetailDto.setTaxAmt(billDetailDto.getTaxAmt().subtract(allowAdjust));
                    lastBillDetail.setTaxAmt(lastBillDetail.getTaxAmt().add(allowAdjust));
                    err = err.subtract(allowAdjust);
                }
            }
        }

        if (hasDisLine) {
            BillDetailDto lastBillDteailDis = detailDtos.get(detailDtos.size() - 1);
            BigDecimal disErr = lastBillDteailDis.getAmounts().multiply(lastBillDteailDis.getTaxRate()).subtract(lastBillDteailDis.getTaxAmt());
            if (disErr.abs().compareTo(lineTaxAmtErr) > 0) {
                BigDecimal adjustAmount = disErr.divide(new BigDecimal(String.valueOf(detailDtos.size())), 2, 0);

                for (BillDetailDto billDetailDto : detailDtos) {
                    if (isDisLine(billDetailDto)) {
                        if (disErr.abs().compareTo(lineAmtErr) <= 0) {
                            break;
                        }

                        BigDecimal allowAdjust = billDetailDto.getAmounts().multiply(billDetailDto.getTaxRate()).subtract(billDetailDto.getTaxAmt()).abs().subtract(new BigDecimal("0.06")).abs().setScale(2, 1);
                        allowAdjust = adjustAmount.compareTo(allowAdjust) > 0 ? allowAdjust : adjustAmount;
                        billDetailDto.setTaxAmt(billDetailDto.getTaxAmt().subtract(allowAdjust));
                        lastBillDteailDis.setTaxAmt(lastBillDteailDis.getTaxAmt().add(allowAdjust));
                        disErr = disErr.subtract(allowAdjust);
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
            allAmounts = amounts;
            if (hasDisLine) {
                allAmounts = amounts.add(detailDtos.get(detailDtos.size() - 1).getAmounts());
            }

            if (priceNumber < 15 && allAmounts.compareTo(invLimitAmt) > 0) {
                configDto.setPriceNumber(priceNumber + 1);
                if (configDto.getPriceNumberType() != 1) {
                    detailDtos = goodsLineSplit((List) originDetailDtos, configDto, originAggrAmt, hasDisLine, allAmount);
                }

                configDto.setPriceNumber(priceNumber);
                return detailDtos;
            }

            if (priceNumber < 15 && priceInc.multiply(amts).subtract(amountsInc).abs().compareTo(lineAmtErr) > 0) {
                configDto.setPriceNumber(priceNumber + 1);
                if (configDto.getPriceNumberType() != 1) {
                    detailDtos = goodsLineSplit((List) originDetailDtos, configDto, originAggrAmt, hasDisLine, allAmount);
                }

                configDto.setPriceNumber(priceNumber);
                return detailDtos;
            }

            if (priceNumber < 15 && price.multiply(amts).subtract(amounts).abs().compareTo(lineAmtErr) > 0) {
                configDto.setPriceNumber(priceNumber + 1);
                if (configDto.getPriceNumberType() != 1) {
                    detailDtos = goodsLineSplit((List) originDetailDtos, configDto, originAggrAmt, hasDisLine, allAmount);
                }

                configDto.setPriceNumber(priceNumber);
                return detailDtos;
            }
        }

        return detailDtos;
    }

    // 判断下一行是否超出了发票限额。它首先获取当前行的金额，然后将其与下一行的金额相加。如果总金额超出了发票限额，则返回 true，否则返回 false
    private static Boolean addNextProLineIsExpeed(List<BillDetailDto> detailDtos, int curIndex, BigDecimal invLimitAmt) {
        BillDetailDto lineDto = detailDtos.get(curIndex);
        BigDecimal countNextAmount = BigDecimal.ZERO.add(lineDto.getAmounts());
        int j = curIndex + 1;
        if (j < detailDtos.size()) {
            BillDetailDto disLineDto = detailDtos.get(j);
            countNextAmount = countNextAmount.add(disLineDto.getAmounts());
        }

        return countNextAmount.compareTo(invLimitAmt) > 0;
    }

    private static void splitLineByDis(SmruleConfigDto configDto, BillDetailDto orginDto, BillDetailDto productDto, BillDetailDto newProductDto, BillDetailDto orginDisDto, BillDetailDto disDto, BillDetailDto newdisDto, BigDecimal aggrAmt, BigDecimal invLimitAmt) throws IllegalAccessException, InvocationTargetException {
        BigDecimal dec = orginDto.getTaxDeduction();
        BigDecimal oAmounts = orginDto.getAmounts();
        BigDecimal oDisAmounts = orginDisDto.getAmounts();
        BigDecimal lineAmtErr = configDto.getLineAmountErr();
        BigDecimal itemAmt = invLimitAmt.subtract(aggrAmt);
        BigDecimal calDisAmt;
        BigDecimal usedAmt;
        if (itemAmt.compareTo(invLimitAmt) > 0) {
            calDisAmt = oDisAmounts.multiply(invLimitAmt).divide(oAmounts, 2, 4);
            usedAmt = invLimitAmt.subtract(calDisAmt);
        } else {
            BigDecimal itemAmtGroup = oAmounts.add(oDisAmounts);
            calDisAmt = oDisAmounts.multiply(itemAmt).divide(itemAmtGroup, 2, 4);
            usedAmt = itemAmt.subtract(calDisAmt);
        }

        int includeTax = orginDto.getIncludeTax();
        Map<String, Integer> decimalMap = getDigitByRuleConfig(configDto);
        int amtNumber = decimalMap.get("amtNumber");
        int priceNumber = decimalMap.get("priceNumber");
        BeanUtils.copyProperties(productDto, orginDto);
        productDto.setAmounts(usedAmt);
        BigDecimal taxRate = productDto.getTaxRate();
        BigDecimal amounts = productDto.getAmounts();
        BigDecimal price = productDto.getPrice();
        BigDecimal price1 = productDto.getPrice();
        BigDecimal priceInc = productDto.getPriceIncTax();
        BigDecimal priceInc1 = productDto.getPriceIncTax();
        BigDecimal amts, amts1;
        BigDecimal taxAmt, taxAmt1;
        BigDecimal amounts1, amountsInc, amountsInc1;
        BeanUtils.copyProperties(newProductDto, orginDto);
        BigDecimal tmpAmountsInc = orginDto.getAmountsIncTax();
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            price = price.setScale(priceNumber, 4);
            if (price.compareTo(BigDecimal.ZERO) == 0) {
                price = price1;
            } else {
                price1 = price1.setScale(priceNumber, 4);
            }

            priceInc = priceInc.setScale(priceNumber, 4);
            if (priceInc.compareTo(BigDecimal.ZERO) == 0) {
                priceInc = priceInc1;
            } else {
                priceInc1 = priceInc1.setScale(priceNumber, 4);
            }

            amts = calcUtilMethods.recursionAmtsCut(amounts, price, amtNumber, configDto);
            amts = amts.setScale(amtNumber, 3);
            if (amts.compareTo(BigDecimal.ZERO) == 0) {
                amts = calcUtilMethods.recursionAmtsCut(amounts, price, amtNumber, configDto);
            }

            if (amts.compareTo(BigDecimal.ONE) <= 0 && price.compareTo(invLimitAmt) < 0 && amts.setScale(amtNumber, 3).compareTo(BigDecimal.ZERO) == 0) {
                BeanUtils.copyProperties(productDto, orginDto);
                newProductDto.setAmounts(BigDecimal.ZERO);
                BeanUtils.copyProperties(disDto, orginDisDto);
                newdisDto.setAmounts(BigDecimal.ZERO);
                return;
            }

            amts1 = orginDto.getAmts().subtract(amts);
            if (amts1.compareTo(BigDecimal.ZERO) < 0) {
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

            amounts = calcUtilMethods.recursionAmounts(amts, price, 2, configDto);
            amounts1 = orginDto.getAmounts().subtract(amounts);
            amountsInc = calcUtilMethods.recursionAmounts(amts, priceInc, 2, configDto);
            amountsInc1 = orginDto.getAmountsIncTax().subtract(amountsInc);
            taxAmt = amountsInc.subtract(amounts);
            taxAmt1 = orginDto.getTaxAmt().subtract(taxAmt);
            if (amounts.compareTo(BigDecimal.ZERO) <= 0 || amounts1.compareTo(BigDecimal.ZERO) < 0 || amountsInc.compareTo(BigDecimal.ZERO) <= 0 || amountsInc1.compareTo(BigDecimal.ZERO) < 0) {
                BeanUtils.copyProperties(productDto, orginDto);
                newProductDto.setAmounts(BigDecimal.ZERO);
                BeanUtils.copyProperties(disDto, orginDisDto);
                newdisDto.setAmounts(BigDecimal.ZERO);
                return;
            }

            if (amounts.multiply(taxRate).subtract(taxAmt).abs().compareTo(configDto.getLineTaxAmtErr()) > 0) {
                taxAmt = calcUtilMethods.calcTaxAmtByNoTaxMoneyDec(amounts, dec, taxRate, 2);
                taxAmt1 = orginDto.getTaxAmt().subtract(taxAmt);
            }

            if (taxRate.compareTo(BigDecimal.ZERO) == 0) {
                taxAmt = orginDto.getTaxAmt().multiply(amounts).divide(orginDto.getAmounts(), 2, 4);
                taxAmt1 = orginDto.getTaxAmt().subtract(taxAmt);
                amountsInc = amounts.add(taxAmt);
                amountsInc1 = orginDto.getAmountsIncTax().subtract(amountsInc);
                if (YOrNEnum.YES.getValue() == includeTax && priceInc.multiply(amts).subtract(amountsInc).abs().compareTo(lineAmtErr) > 0) {
                    amountsInc = calcUtilMethods.recursionAmounts(amts, priceInc, 2, configDto);
                    amountsInc1 = orginDto.getAmountsIncTax().subtract(amountsInc);
                }
            }

            productDto.setAmts(amts);
            productDto.setPrice(price);
            productDto.setPriceIncTax(priceInc);
            newProductDto.setAmts(amts1);
            newProductDto.setPrice(price1);
            newProductDto.setPriceIncTax(priceInc1);
        } else {
            amounts = usedAmt.setScale(2, 4);
            amounts1 = oAmounts.subtract(amounts).setScale(2, 4);
            taxAmt = calcUtilMethods.calcTaxAmtByNoTaxMoneyDec(amounts, dec, taxRate, 2);
            taxAmt1 = orginDto.getTaxAmt().subtract(taxAmt);
            amountsInc = amounts.add(taxAmt);
            amountsInc1 = tmpAmountsInc.subtract(amountsInc).setScale(2, 4);
        }

        productDto.setAmounts(amounts);
        productDto.setAmountsIncTax(amountsInc);
        productDto.setTaxAmt(taxAmt);
        productDto.setSplitSign(1);
        newProductDto.setAmounts(amounts1);
        newProductDto.setAmountsIncTax(amountsInc1);
        newProductDto.setTaxAmt(taxAmt1);
        newProductDto.setSplitSign(1);
        BeanUtils.copyProperties(disDto, orginDisDto);
        BigDecimal disTaxRate = disDto.getTaxRate();
        BigDecimal disAmtount = itemAmt.subtract(amounts);
        disAmtount = disAmtount.compareTo(BigDecimal.ZERO) >= 0 ? calDisAmt : disAmtount;
        BigDecimal disTaxAmt = disAmtount.multiply(disTaxRate).setScale(2, 4);
        disDto.setAmounts(disAmtount);
        disDto.setTaxAmt(disTaxAmt);
        BigDecimal disAmtInc = disAmtount.add(disTaxAmt);
        disDto.setAmountsIncTax(disAmtInc);
        disDto.setSplitSign(1);
        BeanUtils.copyProperties(newdisDto, orginDisDto);
        BigDecimal disAmt1 = orginDisDto.getAmounts().subtract(disAmtount).setScale(2, 4);
        BigDecimal disTaxAmt1 = orginDisDto.getTaxAmt().subtract(disTaxAmt).setScale(2, 4);
        newdisDto.setAmounts(disAmt1);
        newdisDto.setTaxAmt(disTaxAmt1);
        BigDecimal disAmtInc1 = orginDisDto.getAmountsIncTax().subtract(disAmtInc);
        newdisDto.setAmountsIncTax(disAmtInc1);
        newdisDto.setSplitSign(1);
        BillDetailIdSplit(orginDto, productDto, newProductDto, orginDisDto, disDto, newdisDto, configDto);
    }

    private static void splitLine(SmruleConfigDto configDto, BillDetailDto orginDto, BillDetailDto productDto, BillDetailDto newProductDto, BigDecimal aggrAmt, BigDecimal invLimitAmt) throws IllegalAccessException, InvocationTargetException {
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
        BigDecimal price1 = productDto.getPrice();
        BigDecimal priceInc = productDto.getPriceIncTax();
        BigDecimal priceInc1 = productDto.getPriceIncTax();
        BigDecimal amts, amts1, taxAmt, taxAmt1, amounts1, amountsInc, amountsInc1;
        BeanUtils.copyProperties(newProductDto, orginDto);
        BigDecimal tmpAmountsInc = orginDto.getAmountsIncTax();
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            priceInc = priceInc.setScale(priceNumber, 4);
            if (priceInc.compareTo(BigDecimal.ZERO) == 0) {
                priceInc = priceInc1;
            } else {
                priceInc1 = priceInc1.setScale(priceNumber, 4);
            }

            price = price.setScale(priceNumber, 4);
            if (price.compareTo(BigDecimal.ZERO) == 0) {
                price = price1;
            } else {
                price1 = price1.setScale(priceNumber, 4);
            }

            amts = calcUtilMethods.recursionAmtsCut(amounts, price, amtNumber, configDto);
            amts = amts.setScale(amtNumber, 3);
            if (amts.compareTo(BigDecimal.ZERO) == 0) {
                amts = calcUtilMethods.recursionAmtsCut(amounts, price, amtNumber, configDto);
            }

            if (amts.compareTo(BigDecimal.ONE) <= 0 && price.compareTo(invLimitAmt) < 0 && amts.setScale(amtNumber, 3).compareTo(BigDecimal.ZERO) == 0) {
                BeanUtils.copyProperties(productDto, orginDto);
                newProductDto.setAmounts(BigDecimal.ZERO);
                return;
            }

            amts1 = orginDto.getAmts().subtract(amts);
            if (amts1.compareTo(BigDecimal.ZERO) < 0) {
                configDto.setPriceNumber(priceNumber + 1);
                if (configDto.getPriceNumber() > 15) {
                    BeanUtils.copyProperties(productDto, orginDto);
                    newProductDto.setAmounts(BigDecimal.ZERO);
                    return;
                }

                splitLine(configDto, orginDto, productDto, newProductDto, aggrAmt, invLimitAmt);
                return;
            }

            amounts = calcUtilMethods.recursionAmounts(amts, price, 2, configDto);
            amounts1 = orginDto.getAmounts().subtract(amounts);
            amountsInc = calcUtilMethods.recursionAmounts(amts, priceInc, 2, configDto);
            amountsInc1 = orginDto.getAmountsIncTax().subtract(amountsInc);
            taxAmt = amountsInc.subtract(amounts);
            taxAmt1 = orginDto.getTaxAmt().subtract(taxAmt);
            if (amounts.compareTo(BigDecimal.ZERO) <= 0 || amounts1.compareTo(BigDecimal.ZERO) < 0 || amountsInc.compareTo(BigDecimal.ZERO) <= 0 || amountsInc1.compareTo(BigDecimal.ZERO) < 0) {
                BeanUtils.copyProperties(productDto, orginDto);
                newProductDto.setAmounts(BigDecimal.ZERO);
                return;
            }

            if (amounts.multiply(taxRate).subtract(taxAmt).abs().compareTo(configDto.getLineTaxAmtErr()) > 0) {
                taxAmt = calcUtilMethods.calcTaxAmtByNoTaxMoneyDec(amounts, dec, taxRate, 2);
                taxAmt1 = orginDto.getTaxAmt().subtract(taxAmt);
            }

            if (taxRate.compareTo(BigDecimal.ZERO) == 0) {
                taxAmt = orginDto.getTaxAmt().multiply(amounts).divide(orginDto.getAmounts(), 2, 4);
                taxAmt1 = orginDto.getTaxAmt().subtract(taxAmt);
                amountsInc = amounts.add(taxAmt);
                amountsInc1 = orginDto.getAmountsIncTax().subtract(amountsInc);
                if (YOrNEnum.YES.getValue() == includeTax && priceInc.multiply(amts).subtract(amountsInc).abs().compareTo(lineAmtErr) > 0) {
                    amountsInc = calcUtilMethods.recursionAmounts(amts, priceInc, 2, configDto);
                    amountsInc1 = orginDto.getAmountsIncTax().subtract(amountsInc);
                }
            }

            productDto.setAmts(amts);
            productDto.setPrice(price);
            productDto.setPriceIncTax(priceInc);
            newProductDto.setAmts(amts1);
            newProductDto.setPrice(price1);
            newProductDto.setPriceIncTax(priceInc1);
        } else {
            BigDecimal oAmounts = orginDto.getAmounts();
            amounts = usedAmt.setScale(2, 3);
            amounts1 = oAmounts.subtract(amounts).setScale(2, 3);
            taxAmt = calcUtilMethods.calcTaxAmtByNoTaxMoneyDec(amounts, dec, taxRate, 2);
            taxAmt1 = orginDto.getTaxAmt().subtract(taxAmt);
            amountsInc = amounts.add(taxAmt);
            amountsInc1 = tmpAmountsInc.subtract(amountsInc).setScale(2, 4);
        }

        productDto.setAmounts(amounts);
        productDto.setAmountsIncTax(amountsInc);
        productDto.setTaxAmt(taxAmt);
        productDto.setSplitSign(1);
        newProductDto.setAmounts(amounts1);
        newProductDto.setAmountsIncTax(amountsInc1);
        newProductDto.setTaxAmt(taxAmt1);
        newProductDto.setSplitSign(1);
        BillDetailIdSplit(orginDto, productDto, newProductDto, null, null, null, configDto);
    }

    public static Map<String, Integer> getDigitByRuleConfig(SmruleConfigDto configDto) {
        Map<String, Integer> retMap = new HashMap<>(1);
        Integer amtNumberType = configDto.getAmtNumberType();
        Integer amtNumber = configDto.getAmtNumber();
        Integer priceNumberType = configDto.getPriceNumberType();
        Integer priceNumber = configDto.getPriceNumber();
        int digit = InvoiceConstant.IS_TAX_YES_INT;
        if (NumberTypeEnum.ZERO.getValue().equals(priceNumberType)) {
            digit = priceNumber;
        } else if (NumberTypeEnum.ONE.getValue().equals(priceNumberType)) {
            digit = 0;
        } else if (NumberTypeEnum.TWO.getValue().equals(priceNumberType)) {
            digit = priceNumber;
        }

        retMap.put("priceNumber", digit);
        if (NumberTypeEnum.ZERO.getValue().equals(amtNumberType)) {
            digit = amtNumber;
        } else if (NumberTypeEnum.ONE.getValue().equals(amtNumberType)) {
            digit = 0;
        } else if (NumberTypeEnum.TWO.getValue().equals(amtNumberType)) {
            digit = amtNumber;
        }

        retMap.put("amtNumber", digit);
        return retMap;
    }

    public static boolean isDisLine(BillDetailDto detailDto) {
        Integer lineProperty = detailDto.getLineProperty();
        BigDecimal amounts = detailDto.getAmounts();
        boolean isDisline = false;
        if (LinePropertyEnum.FOUR.getValue().compareTo(lineProperty) == 0 && amounts.compareTo(BigDecimal.ZERO) < 0) {
            isDisline = true;
        }

        return isDisline;
    }

    public static void BillDetailIdSplit(BillDetailDto orginDto, BillDetailDto productDto, BillDetailDto newProductDto, BillDetailDto orginDisDto, BillDetailDto disDto, BillDetailDto newdisDto, SmruleConfigDto configDto) {
        Set<BillDetailIdDto> orginDetailIdSet = orginDto.getDetailIdSet();
        Iterator<BillDetailIdDto> it = orginDetailIdSet.iterator();
        Set<BillDetailIdDto> productDetailIdSet = new LinkedHashSet<>();
        BigDecimal sumAmount = BigDecimal.ZERO;

        BigDecimal sumDisAmount;
        while (it.hasNext()) {
            BillDetailIdDto billDetailIdDto = it.next();
            sumAmount = sumAmount.add(billDetailIdDto.getAmounts());
            if (sumAmount.compareTo(productDto.getAmountsByTax()) > 0) {
                BigDecimal splitAomunt2 = sumAmount.subtract(productDto.getAmountsByTax());
                BigDecimal splitAomunt1 = billDetailIdDto.getAmounts().subtract(splitAomunt2);
                sumDisAmount = calcUtilMethods.recursionAmtsCut(splitAomunt2, billDetailIdDto.getPrice(), configDto.getAmtNumber(), configDto);
                BigDecimal amt1 = billDetailIdDto.getAmts().subtract(sumDisAmount);
                BillDetailIdDto splitBillDetailId1 = new BillDetailIdDto(billDetailIdDto.getBillNO(), billDetailIdDto.getBillDetailNO(), splitAomunt1, billDetailIdDto.getPrice(), amt1);
                BillDetailIdDto splitBillDetailId2 = new BillDetailIdDto(billDetailIdDto.getBillNO(), billDetailIdDto.getBillDetailNO(), splitAomunt2, billDetailIdDto.getPrice(), sumDisAmount);
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
        if (orginDisDto != null) {
            Set<BillDetailIdDto> orginDisDetailIdSet = orginDisDto.getDetailIdSet();
            Iterator<BillDetailIdDto> disIt = orginDisDetailIdSet.iterator();
            Set<BillDetailIdDto> disProductDetailIdSet = new LinkedHashSet<>();
            sumDisAmount = BigDecimal.ZERO;

            while (disIt.hasNext()) {
                BillDetailIdDto billDetailIdDto = disIt.next();
                sumDisAmount = sumDisAmount.add(billDetailIdDto.getAmounts());
                if (sumDisAmount.compareTo(disDto.getAmountsByTax()) < 0) {
                    BigDecimal splitAomunt2 = sumDisAmount.subtract(disDto.getAmountsByTax());
                    BigDecimal splitAomunt1 = billDetailIdDto.getAmounts().subtract(splitAomunt2);
                    BillDetailIdDto splitBillDetailId1 = new BillDetailIdDto(billDetailIdDto.getBillNO(), billDetailIdDto.getBillDetailNO(), splitAomunt1, billDetailIdDto.getPrice(), billDetailIdDto.getAmts());
                    BillDetailIdDto splitBillDetailId2 = new BillDetailIdDto(billDetailIdDto.getBillNO(), billDetailIdDto.getBillDetailNO(), splitAomunt2, billDetailIdDto.getPrice(), billDetailIdDto.getAmts());
                    disProductDetailIdSet.add(splitBillDetailId1);
                    disIt.remove();
                    orginDisDetailIdSet.add(splitBillDetailId2);
                    break;
                }

                if (sumDisAmount.compareTo(disDto.getAmountsByTax()) == 0) {
                    disProductDetailIdSet.add(billDetailIdDto);
                    disIt.remove();
                    break;
                }

                disProductDetailIdSet.add(billDetailIdDto);
                disIt.remove();
            }

            disDto.setDetailIdSet(disProductDetailIdSet);
            newdisDto.setDetailIdSet(orginDisDetailIdSet);
        }
    }
}