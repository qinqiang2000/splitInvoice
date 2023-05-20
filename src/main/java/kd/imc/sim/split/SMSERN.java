package kd.imc.sim.split;

import kd.imc.sim.split.dto.*;
import kd.imc.sim.split.enums.EnumType;
import kd.imc.sim.split.exception.EtcRuleException;
import kd.imc.sim.split.methods.BillCheckMethods;
import kd.imc.sim.split.methods.InvoiceMethods;
import kd.imc.sim.split.service.BillsCheckService;
import kd.imc.sim.split.service.GoodsDiscountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;


public class SMSERN {
    //private static Log logger = LogFactory.getLog(SMSERN.class);
    private static Logger logger = LoggerFactory.getLogger(SMSERN.class);
    static InvoiceMethods invoiceMethods = new InvoiceMethods();
    static BillCheckMethods billCheckMethods = new BillCheckMethods();

    public SmsResultDto doSMSER(SmsRequestDto requestDto) {
        SmsResultDto resultDto = new SmsResultDto();
        try {
            // 校验传入单据是否为空
            if (requestDto.getBillSList() == null || requestDto.getBillSList().size() == 0) {
                throw new EtcRuleException("未传入需处理单据");
            }
            // 构建拆分规则
            buildSmr(requestDto);
            // 校验票种与限额传入
            billCheckMethods.checkBill(requestDto, resultDto);
            // 拆分单据
            int failNums = ruleBillSubjects(requestDto, resultDto);
            resultDto.setTotals(1);
            resultDto.setDoSucc(1 - failNums);
            resultDto.setDoFail(failNums);
            return resultDto;
        } catch (Exception e) {
            logger.error("单据拆分处理异常", e);
            resultDto.setSuccess(false);
            resultDto.setErrorMsg(e.getMessage());
            return resultDto;
        }
    }

    // 构建拆分规则
    private void buildSmr(SmsRequestDto requestDto) {
        if (requestDto.getSmr() == null) {
            requestDto.setSmr(new SmruleConfigDto());
        }
        
        SmruleConfigDto smr = requestDto.getSmr();
        // 1调整金额（弃用） 2 单价不变调整数量 3 总数量不变调整单价 
        if (requestDto.getSmr().getSplitGoodsWithNumber() == 1) {
            smr.setSplitGoodsWithNumber(2);
        }
        smr.setSplitListType(1);
    }

    private int ruleBillSubjects(SmsRequestDto requestDto, SmsResultDto resultDto) {
        BillSubjectDto bill = requestDto.getBillSList().get(0);
        if (!bill.getCheckPassed()) {
            return 1;
        }
        try {
            SmruleConfigDto smruleConfigDto = requestDto.getSmr();
            // 根据发票种类，设置限额
            smruleConfigDto.setInvLimitAmt(getInvLimitAmt(bill.getInvKind(), requestDto));

            bill.setIsOil(requestDto.getIsOil());
            tranferRule(bill, smruleConfigDto, resultDto);
            return 0;
        } catch (Exception e) {
            logger.error("拆合异常", e);
            BillDealResultDto dealResultDto = BillsCheckService.getBDR(bill.getBillNO(), false, e.getMessage());
            resultDto.getBdrList().add(dealResultDto);
            return 1;
        }
    }

    private void tranferRule(BillSubjectDto subjectDto, SmruleConfigDto smruleConfigDto, SmsResultDto resultDto) throws EtcRuleException {
        // 校验单据明细
        BillsCheckService.billItemsCheck(subjectDto, smruleConfigDto);
        // 获取单张发票最大行数
        invoiceMethods.getMaxLine(subjectDto, smruleConfigDto);
        // 处理折扣行
        List<BillDetailDto> billDetailDtos = GoodsDiscountService.doDisLine(subjectDto.getBillDList());
        subjectDto.setBillDList(billDetailDtos);
        // 计算总金额、税额；设置最终限额金额
        invoiceMethods.invoiceLimit(subjectDto, smruleConfigDto);
        // 拆分发票
        invoiceMethods.genInvoice(subjectDto, smruleConfigDto, resultDto);
        // 校验拆分后的发票
        invoiceMethods.taxAmtCheck(subjectDto, smruleConfigDto, resultDto);
    }

    private BigDecimal getInvLimitAmt(int preInvKind, SmsRequestDto requestDto) {
        BigDecimal invLimitAmt;
        if (EnumType.InvKindEnum.SPECIAL.getValue() == preInvKind) {
            invLimitAmt = requestDto.getSiAmt();
        } else if (EnumType.InvKindEnum.NORMAL.getValue() == preInvKind) {
            invLimitAmt = requestDto.getCiAmt();
        } else if (EnumType.InvKindEnum.ROLL.getValue() == preInvKind) {
            invLimitAmt = requestDto.getVlAmt();
        } else if (EnumType.InvKindEnum.ESINV.getValue() == preInvKind) {
            invLimitAmt = requestDto.getEsiAmt();
        } else {
            invLimitAmt = requestDto.getEiAmt();
        }

        return invLimitAmt;
    }
}