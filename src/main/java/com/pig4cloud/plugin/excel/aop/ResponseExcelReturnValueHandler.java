package com.pig4cloud.plugin.excel.aop;


import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.pig4cloud.plugin.excel.handler.SheetWriteHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 处理@ResponseExcel 返回值
 *
 * @author lengleng
 */
@Slf4j
@RequiredArgsConstructor
public class ResponseExcelReturnValueHandler implements HandlerMethodReturnValueHandler {
	private final List<SheetWriteHandler> sheetWriteHandlerList;


	/**
	 * 只处理@ResponseExcel 声明的方法
	 *
	 * @param parameter 方法签名
	 * @return 是否处理
	 */
	@Override
	public boolean supportsReturnType(MethodParameter parameter) {
		return parameter.getMethodAnnotation(ResponseExcel.class) != null;
	}

	/**
	 * 处理逻辑
	 *
	 * @param o                返回参数
	 * @param parameter        方法签名
	 * @param mavContainer     上下文容器
	 * @param nativeWebRequest 上下文
	 * @throws Exception 处理异常
	 */
	@Override
	public void handleReturnValue(Object o, MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest nativeWebRequest) throws Exception {
		/* check */
		HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);
		Assert.state(response != null, "No HttpServletResponse");
		ResponseExcel responseExcel = parameter.getMethodAnnotation(ResponseExcel.class);
		Assert.state(responseExcel != null, "No @ResponseExcel");
		mavContainer.setRequestHandled(true);

		/* return value check */
		if (!(o instanceof List)) {
			String msg = "return value is null or not support type, can not build excel";
			log.warn(msg);
			response.setContentType(MediaType.TEXT_PLAIN_VALUE);
			response.getWriter().write(msg);
			response.getWriter().flush();
			return;
		}

		sheetWriteHandlerList.forEach(handler -> {
			if (handler.support(o)) {
				handler.export(o, response, responseExcel);
			}
		});
	}
}
