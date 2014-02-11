class TestClassAdd():
    def inc_flow(self, flow_id, cookie_id):
        raise NotImplementedError("inc_flow is not implemented")

    def inc_error(self):
        raise NotImplementedError("inc_error is not implemented")

class TestClassRemove():
    def delete_flow_from_map(self, flow_id, cookie_id):
        raise NotImplementedError("inc_flow is not implemented")

