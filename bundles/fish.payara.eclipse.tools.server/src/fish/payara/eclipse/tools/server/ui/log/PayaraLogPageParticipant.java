/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.ui.log;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;

public class PayaraLogPageParticipant implements IConsolePageParticipant {

    @Override
    public void init(IPageBookViewPage page, IConsole console) {
        if (page.getControl() instanceof StyledText) {
            StyledText viewer = (StyledText) page.getControl();
            viewer.addLineStyleListener(new LogStyle(((TextConsole) console).getDocument()));
        }
    }

    @Override
    public void activated() {
    }

    @Override
    public void deactivated() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

}
