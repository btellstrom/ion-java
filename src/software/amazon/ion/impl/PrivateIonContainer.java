/*
 * Copyright 2010-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at:
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package software.amazon.ion.impl;

import software.amazon.ion.IonContainer;
import software.amazon.ion.IonValue;

/**
 * Internal, private, interfaces for manipulating
 * the base child collection of IonContainer
 *
 * @deprecated This is an internal API that is subject to change without notice.
 */
@Deprecated
public interface PrivateIonContainer
    extends IonContainer
{
    public int      get_child_count();
    public IonValue get_child(int idx);
}
