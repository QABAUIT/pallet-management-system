import apiClient from '../api/apiClient';

const base = '/bao-cao-tai-che';
const query = ({ tuNgay, denNgay }) => ({ params: { tu: tuNgay, den: denNgay } });

// Hàm cấp module (ổn định) để useFinanceReport dùng làm dependency.
// Trả về { data } giống các hàm trong taiChinhService.
export const baoCaoTaiCheService = {
  tongHop: async (range) => {
    const [hieuSuat, theoLo, chenhLech] = await Promise.all([
      apiClient.get(`${base}/hieu-suat`, query(range)),
      apiClient.get(`${base}/theo-lo`, query(range)),
      apiClient.get(`${base}/chenh-lech-dinh-muc`, query(range)),
    ]);
    return { data: { hieuSuat: hieuSuat.data, theoLo: theoLo.data, chenhLech: chenhLech.data } };
  },
};
