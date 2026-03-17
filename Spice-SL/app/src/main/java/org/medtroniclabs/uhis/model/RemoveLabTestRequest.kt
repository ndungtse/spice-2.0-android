package org.medtroniclabs.uhis.model

import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto

class RemoveLabTestRequest(val id: String, val provenance: ProvanceDto = ProvanceDto())
