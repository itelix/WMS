package cma.store.control.opt;

import java.util.List;

import cma.store.control.BaseRealization;
import cma.store.control.Realization;
import cma.store.data.Bot;
import cma.store.exception.NoFreeBotException;
import cma.store.input.request.BaseRequest;

public interface Realizer {
	
	String getAlgorithmName();
	
    Realization findRealization( List<BaseRequest> baseRequestList ) throws RealizationException;
}
