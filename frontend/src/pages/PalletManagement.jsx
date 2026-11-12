import React from "react";
import { Skeleton, Flex, Typography } from "antd";
import { Lightbulb } from "lucide-react";
import Pagination from "../components/common/Pagination";
import PalletHeader from "../components/pallet/PalletHeader";
import PalletCard from "../components/pallet/PalletCard";
import PalletDetailDrawer from "../components/pallet/PalletDetailDrawer";
import PalletModal from "../components/pallet/PalletModal";
import { usePallets } from "../hooks/usePallets";
import { exportToCsv } from "../utils/exportCsv";

const { Text } = Typography;

export default function PalletManagement() {
  const {
    loading,
    searchText,
    setSearchText,
    filter,
    setFilter,
    filterTab,
    setFilterTab,
    currentPage,
    setCurrentPage,
    paginatedPallets,
    filteredPallets,
    totalItems,
    selectedPallet,
    setSelectedPallet,
    isModalVisible,
    setIsModalVisible,
    modalMode,
    form,
    handleAdd,
    handleEdit,
    handleDelete,
    handleModalSubmit,
  } = usePallets(8);

  const defaultFilter = { loaiMatHang: [], chatLieu: [] };

  const handleExport = () => {
    // Export the filtered list, not just the current page
    exportToCsv(`Danh_sach_pallet_${new Date().getTime()}.csv`, filteredPallets);
  };

  return (
    <Flex vertical className="page-shell">
      <PalletHeader
        searchText={searchText}
        setSearchText={setSearchText}
        filter={filter}
        setFilter={setFilter}
        filterTab={filterTab}
        setFilterTab={setFilterTab}
        defaultFilter={defaultFilter}
        totalPallets={totalItems}
        onAdd={handleAdd}
        onExport={handleExport}
      />

      <Flex vertical style={{ flex: 1 }}>
        <div className="pallet-heading-row">
          <Text strong className="page-heading-title">
            DANH MỤC SẢN PHẨM
          </Text>
          <span className="count-badge">
            Hiển thị {paginatedPallets.length}/{totalItems}
          </span>
          <span className="page-hint pallet-hint">
            <Lightbulb size={15} aria-hidden />
            Bấm thẻ để xem chi tiết đầy đủ bên phải
          </span>
        </div>

        <div className="pallet-grid">
          {loading ? (
            <Skeleton active paragraph={{ rows: 4 }} />
          ) : (
            paginatedPallets.map((pallet) => (
              <PalletCard
                key={pallet.id}
                pallet={pallet}
                isSelected={selectedPallet?.id === pallet.id}
                onClick={() =>
                  setSelectedPallet(
                    selectedPallet?.id === pallet.id ? null : pallet,
                  )
                }
              />
            ))
          )}
        </div>

        <div className="pagination-row">
          <Pagination
            currentPage={currentPage}
            totalItems={totalItems}
            itemsPerPage={8}
            itemLabel="pallet"
            onPageChange={setCurrentPage}
          />
        </div>

        <PalletDetailDrawer
          selectedPallet={selectedPallet}
          onClose={() => setSelectedPallet(null)}
          onEdit={() => handleEdit(selectedPallet)}
          onDelete={() => handleDelete(selectedPallet.id)}
        />

        <PalletModal
          isModalVisible={isModalVisible}
          setIsModalVisible={setIsModalVisible}
          modalMode={modalMode}
          form={form}
          handleModalSubmit={handleModalSubmit}
        />
      </Flex>
    </Flex>
  );
}
