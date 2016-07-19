package edu.ucsd.sbrg.escher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Created by devkhan on 19/07/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodCallsTest {

  @Test
  public void mockConverter() {
    //      EscherConverter mock = mock(EscherConverter.class);
    EscherConverter converter = spy(new EscherConverter());
    Mockito.verify(converter, Mockito.times(1)).commandLineMode(any());

  }
}
