/*
 * Copyright © 2013 VillageReach. All Rights Reserved. This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 *
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.openlmis.allocation.repository;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.allocation.domain.DeliveryZone;
import org.openlmis.allocation.repository.mapper.DeliveryZoneMapper;
import org.openlmis.db.categories.UnitTests;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.domain.Right.PLAN_DISTRIBUTION;

@Category(UnitTests.class)
@RunWith(MockitoJUnitRunner.class)
public class DeliveryZoneRepositoryTest {

  @Mock
  DeliveryZoneMapper mapper;

  @InjectMocks
  DeliveryZoneRepository repository;

  @Test
  public void shouldInsertDeliveryZone() throws Exception {
    DeliveryZone zone = new DeliveryZone();

    repository.insert(zone);

    verify(mapper).insert(zone);
  }

  @Test
  public void shouldUpdateDeliveryZone() throws Exception {
    DeliveryZone zone = new DeliveryZone();

    repository.update(zone);

    verify(mapper).update(zone);
  }

  @Test
  public void shouldGetDeliveryZoneByCode() throws Exception {
    DeliveryZone zone = new DeliveryZone();
    when(mapper.getByCode("code")).thenReturn(zone);

    DeliveryZone returnedZone = repository.getByCode("code");

    verify(mapper).getByCode("code");
    assertThat(returnedZone, is(zone));
  }

  @Test
  public void shouldGetAllDeliveryZonesForAUser() throws Exception {
    List<DeliveryZone> deliveryZones = new ArrayList<>();
    when(mapper.getByUserForRight(1l, PLAN_DISTRIBUTION)).thenReturn(deliveryZones);

    List<DeliveryZone> returnedZones = repository.getByUserForRight(1l, PLAN_DISTRIBUTION);

    verify(mapper).getByUserForRight(1l, PLAN_DISTRIBUTION);
    assertThat(returnedZones, is(deliveryZones));
  }
}
